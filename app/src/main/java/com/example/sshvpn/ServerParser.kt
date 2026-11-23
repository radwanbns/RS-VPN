package com.example.sshvpn

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import org.yaml.snakeyaml.Yaml
import java.net.URI
import java.net.URLDecoder

data class Server(
    val host: String,
    val port: Int = 22,
    val user: String = "root",
    val password: String? = null,
    val name: String = host
)

object ServerParser {

    fun parse(input: String): List<Server> {
        val text = input.trim()
        if (text.isEmpty()) return emptyList()

        val content = decodeBase64OrNull(text) ?: text

        return when {
            content.startsWith("{") || content.startsWith("[") -> parseJson(content)
            content.lines().any { it.trim().startsWith("ssh://", ignoreCase = true) } -> parseSshUris(content)
            looksLikeYaml(content) -> parseYaml(content)
            else -> parseLines(content)
        }
    }

    // ---------- Base64 ----------

    private fun decodeBase64OrNull(text: String): String? {
        val compact = text.filterNot { it.isWhitespace() }
        if (compact.length < 8 || !Regex("^[A-Za-z0-9+/_-]+=*$").matches(compact)) return null
        return try {
            val bytes = try {
                Base64.decode(compact, Base64.DEFAULT)
            } catch (e: IllegalArgumentException) {
                Base64.decode(compact, Base64.URL_SAFE)
            }
            val decoded = String(bytes, Charsets.UTF_8).trim()
            val looksValid = decoded.contains("ssh://") ||
                    decoded.startsWith("{") || decoded.startsWith("[") ||
                    decoded.contains(",") || decoded.contains("host")
            if (looksValid) decoded else null
        } catch (e: Exception) {
            null
        }
    }

    // ---------- اكتشاف الصيغة ----------

    private fun looksLikeYaml(text: String): Boolean {
        val keyPattern = Regex("^(host|servers|name|user|port|password)\\s*:.*$")
        return text.lines().any { line ->
            val t = line.trim()
            t.startsWith("- ") || keyPattern.matches(t)
        }
    }

    // ---------- بناء الخادم مع القيم الافتراضية ----------

    private fun makeServer(
        host: String?,
        port: String?,
        user: String?,
        password: String?,
        name: String?
    ): Server? {
        val h = host?.trim().orEmpty()
        if (h.isEmpty()) return null
        return Server(
            host = h,
            port = port?.trim()?.toIntOrNull() ?: 22,
            user = user?.trim()?.takeIf { it.isNotEmpty() } ?: "root",
            password = password?.takeIf { it.isNotEmpty() },
            name = name?.trim()?.takeIf { it.isNotEmpty() } ?: h
        )
    }

    // ---------- JSON ----------

    private fun parseJson(text: String): List<Server> {
        val array = if (text.startsWith("[")) {
            JSONArray(text)
        } else {
            val obj = JSONObject(text)
            obj.optJSONArray("servers") ?: JSONArray().put(obj)
        }
        return (0 until array.length()).mapNotNull { i ->
            val item = array.optJSONObject(i) ?: return@mapNotNull null
            makeServer(
                host = item.optString("host"),
                port = item.optString("port", "22"),
                user = item.optString("user", "root"),
                password = item.optString("password", ""),
                name = item.optString("name", "")
            )
        }
    }

    // ---------- YAML ----------

    @Suppress("UNCHECKED_CAST")
    private fun parseYaml(text: String): List<Server> {
        val root: Any = Yaml().load<Any>(text) ?: return emptyList()
        val items: List<*> = when (root) {
            is List<*> -> root
            is Map<*, *> -> (root["servers"] as? List<*>) ?: listOf(root)
            else -> emptyList<Any>()
        }
        return items.mapNotNull { item ->
            val m = item as? Map<String, Any?> ?: return@mapNotNull null
            makeServer(
                host = m["host"]?.toString(),
                port = m["port"]?.toString(),
                user = m["user"]?.toString(),
                password = m["password"]?.toString(),
                name = m["name"]?.toString()
            )
        }
    }

    // ---------- ssh:// ----------

    private fun parseSshUris(text: String): List<Server> =
        text.lines().mapNotNull { parseSshUri(it) }

    fun parseSshUri(line: String): Server? {
        val trimmed = line.trim()
        if (!trimmed.startsWith("ssh://", ignoreCase = true)) return null
        return try {
            val uri = URI(trimmed)
            val host = uri.host ?: return null
            val info = uri.rawUserInfo
            val user = info?.substringBefore(":")?.let { URLDecoder.decode(it, "UTF-8") }
            val password = if (info != null && info.contains(":")) {
                URLDecoder.decode(info.substringAfter(":"), "UTF-8")
            } else null
            makeServer(
                host = host,
                port = if (uri.port == -1) null else uri.port.toString(),
                user = user,
                password = password,
                name = host
            )
        } catch (e: Exception) {
            null
        }
    }

    // ---------- سطور نصية: host,port,user,password ----------

    private fun parseLines(text: String): List<Server> =
        text.lines().mapNotNull { raw ->
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#")) return@mapNotNull null
            val parts = line.split(",").map { it.trim() }
            makeServer(
                host = parts.getOrNull(0),
                port = parts.getOrNull(1),
                user = parts.getOrNull(2),
                password = parts.getOrNull(3),
                name = null
            )
        }
}
