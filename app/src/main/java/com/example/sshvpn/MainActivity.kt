package com.example.sshvpn

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.DataInputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var updateButton: Button
    private lateinit var statusText: TextView
    private lateinit var serverList: ListView
    private lateinit var disconnectButton: Button

    private val io = Executors.newCachedThreadPool()
    private val ui = Handler(Looper.getMainLooper())

    private var servers: List<Server> = emptyList()
    private var sshClient: SSHClient? = null
    private var socksServer: ServerSocket? = null
    private lateinit var adapter: ArrayAdapter<String>

    companion object {
        private const val SOCKS_PORT = 1080
        private const val PING_TIMEOUT_MS = 3000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlInput = findViewById(R.id.urlInput)
        updateButton = findViewById(R.id.updateButton)
        statusText = findViewById(R.id.statusText)
        serverList = findViewById(R.id.serverList)
        disconnectButton = findViewById(R.id.disconnectButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        serverList.adapter = adapter

        updateButton.setOnClickListener {
            val url = urlInput.text.toString().trim()
            fetchServers(url)
        }

        serverList.setOnItemClickListener { _, _, position, _ ->
            servers.getOrNull(position)?.let { connect(it) }
        }

        disconnectButton.setOnClickListener { disconnect() }
    }

    override fun onDestroy() {
        disconnect()
        io.shutdown()
        super.onDestroy()
    }

    private fun setStatus(text: String) {
        ui.post { statusText.text = text }
    }

    // ---------- تحديث القائمة من HTTPS ----------

    private fun fetchServers(url: String) {
        if (!url.startsWith("https://")) {
            setStatus("يجب أن يبدأ الرابط بـ https://")
            return
        }
        setStatus("جارٍ تحميل القائمة...")
        io.execute {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000
                conn.requestMethod = "GET"
                if (conn.responseCode !in 200..299) {
                    setStatus("فشل التحميل، رمز الاستجابة: ${conn.responseCode}")
                    return@execute
                }
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val parsed = ServerParser.parse(text)
                if (parsed.isEmpty()) {
                    setStatus("لم يتم العثور على خوادم صالحة في القائمة")
                    return@execute
                }
                servers = parsed
                ui.post {
                    adapter.clear()
                    adapter.addAll(parsed.map { "${it.name} (${it.host}:${it.port})" })
                    adapter.notifyDataSetChanged()
                }
                setStatus("تم تحميل ${parsed.size} خادم، جارٍ اختبار زمن الاستجابة...")
                selectFastestAndConnect(parsed)
            } catch (e: Exception) {
                setStatus("خطأ أثناء التحديث: ${e.message}")
            }
        }
    }

    // ---------- اختبار زمن الاستجابة ----------

    private fun pingServer(server: Server): Long? {
        return try {
            Socket().use { socket ->
                val start = System.nanoTime()
                socket.connect(InetSocketAddress(server.host, server.port), PING_TIMEOUT_MS)
                (System.nanoTime() - start) / 1_000_000
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun selectFastestAndConnect(list: List<Server>) {
        val results = list.map { it to pingServer(it) }
            .filter { it.second != null }
            .sortedBy { it.second }

        if (results.isEmpty()) {
            setStatus("لا يمكن الوصول إلى أي خادم")
            return
        }
        val (best, latency) = results.first()
        setStatus("أسرع خادم: ${best.name} (${latency} ms)، جارٍ الاتصال...")
        connect(best)
    }

    // ---------- الاتصال عبر SSH ----------

    private fun connect(server: Server) {
        io.execute {
            disconnectInternal()
            try {
                setStatus("جارٍ الاتصال بـ ${server.host}...")
                val client = SSHClient()
                // تحذير: هذا يتجاهل التحقق من مفتاح الخادم. استبدله بمُتحقق مناسب في الإنتاج.
                client.addHostKeyVerifier(PromiscuousVerifier())
                client.connectTimeout = 10_000
                client.connect(server.host, server.port)
                client.authPassword(server.user, server.password ?: "")

                sshClient = client
                startSocksServer(client)
                setStatus("متصل بـ ${server.host}. SOCKS5 يعمل على 127.0.0.1:$SOCKS_PORT")
            } catch (e: Exception) {
                disconnectInternal()
                setStatus("فشل الاتصال: ${e.message}")
            }
        }
    }

    private fun startSocksServer(client: SSHClient) {
        val ss = ServerSocket()
        ss.reuseAddress = true
        ss.bind(InetSocketAddress(InetAddress.getByName("127.0.0.1"), SOCKS_PORT))
        socksServer = ss

        io.execute {
            while (!ss.isClosed) {
                val incoming = try {
                    ss.accept()
                } catch (e: Exception) {
                    break
                }
                io.execute { handleSocksClient(incoming, client) }
            }
        }
    }

    // ---------- خادم SOCKS5 بسيط (CONNECT فقط) ----------

    private fun handleSocksClient(client: Socket, sshClient: SSHClient) {
        client.use { socket ->
            try {
                val input = DataInputStream(socket.getInputStream())
                val output = socket.getOutputStream()

                // التحية: VER, NMETHODS, METHODS
                val version = input.readUnsignedByte()
                val nMethods = input.readUnsignedByte()
                repeat(nMethods) { input.readUnsignedByte() }
                if (version != 5) return
                output.write(byteArrayOf(5, 0)) // بدون مصادقة
                output.flush()

                // الطلب: VER, CMD, RSV, ATYP, DST.ADDR, DST.PORT
                input.readUnsignedByte()
                val cmd = input.readUnsignedByte()
                input.readUnsignedByte()
                val atyp = input.readUnsignedByte()
                val host = when (atyp) {
                    1 -> {
                        val b = ByteArray(4); input.readFully(b)
                        InetAddress.getByAddress(b).hostAddress
                    }
                    3 -> {
                        val len = input.readUnsignedByte()
                        val b = ByteArray(len); input.readFully(b)
                        String(b, Charsets.UTF_8)
                    }
                    4 -> {
                        val b = ByteArray(16); input.readFully(b)
                        InetAddress.getByAddress(b).hostAddress
                    }
                    else -> return
                }
                val port = input.readUnsignedShort()

                if (cmd != 1) { // ليس CONNECT
                    output.write(byteArrayOf(5, 7, 0, 1, 0, 0, 0, 0, 0, 0))
                    return
                }

                val remote = try {
                    sshClient.newDirectConnection(host, port)
                } catch (e: Exception) {
                    output.write(byteArrayOf(5, 5, 0, 1, 0, 0, 0, 0, 0, 0)) // رفض الاتصال
                    return
                }

                output.write(byteArrayOf(5, 0, 0, 1, 0, 0, 0, 0, 0, 0)) // نجاح
                output.flush()

                remote.use { relay(socket, it) }
            } catch (e: Exception) {
                // يتم تجاهل أخطاء العملاء الفردية
            }
        }
    }

    private fun relay(client: Socket, remote: Socket) {
        val upstream = Thread { copy(client.getInputStream(), remote.getOutputStream()) }
        upstream.start()
        copy(remote.getInputStream(), client.getOutputStream())
        upstream.join()
    }

    private fun copy(input: InputStream, output: OutputStream) {
        try {
            val buffer = ByteArray(8192)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                output.write(buffer, 0, n)
                output.flush()
            }
        } catch (e: Exception) {
            // الاتصال مغلق
        }
    }

    // ---------- قطع الاتصال ----------

    private fun disconnect() {
        io.execute {
            disconnectInternal()
            setStatus("تم قطع الاتصال")
        }
    }

    private fun disconnectInternal() {
        try { socksServer?.close() } catch (_: Exception) {}
        socksServer = null
        try { sshClient?.disconnect() } catch (_: Exception) {}
        sshClient = null
    }
}
