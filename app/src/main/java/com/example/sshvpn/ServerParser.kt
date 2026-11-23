package com.example.sshvpn

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import org.yaml.snakeyaml.Yaml
import java.net.URI
import java.net.URLDecoder

data class Server(
    val name: String,
    val host: String,
    val port: Int = 22,
    val user: String = "root",
    val password: String = ""
)

object ServerParser {

    fun parse(raw: String): List<Server> {
        val text = decodeBase64IfNeeded(raw.trim()).trim()
        return when {
            text.startsWith("{") || text.startsWith("[") -> parseJson(text)
            text.startsWith("servers:") || text.startsWith("- ") -> parseYaml(text)
            else -> parseLines(text)
        }
    }

    private fun decodeBase64IfNeeded(s: String): String = try {
        val decoded = String(Base64.decode(s, Base64.DEFAULT), Charsets.UTF_8)
        if (decoded.contains("ssh://") || decoded.contains(",") || decoded.trimStart().startsWith("{")) {
            decoded
        } else {
            s
        }
    } catch (e: Exception) {
        s
    }

    private fun parseJson(text: String): List<Server> {
        val arr = if (text.startsWith("[")) {
            JSONArray(text)
        } else {
            JSONObject(text).optJSONArray("servers") ?: JSONArray().put(JSONObject(text))
        }
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val host = o.getString("host")
            Server(
                name = o.optString("name", host),
                host = host,
                port = o.optInt("port", 22),
                user = o.optString("user", "root"),
                password = o.optString("password", "")
            )
        }
    }

    private fun parseYaml(text: String): List<Server> {
        val data = Yaml().load<Any?>(text)
        val items: List<*> = when (data) {
            is Map<*, *> -> (data["servers"] as? List<*>) ?: listOf(data)
            is List<*> -> data
            else -> emptyList<Any>()
        }
        return items.filterIsInstance<Map<*, *>>().mapNotNull { m ->
            val host = m["host"]?.toString() ?: return@mapNotNull null
            Server(
                name = m["name"]?.toString() ?: host,
                host = host,
                port = (m["port"] as? Number)?.toInt() ?: 22,
                user = m["user"]?.toString() ?: "root",
                password = m["password"]?.toString() ?: ""
            )
        }
    }

    private fun parseLines(text: String): List<Server> =
        text.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { line ->
                if (line.startsWith("ssh://")) {
                    parseSshUri(line)
                } else {
                    // الصيغة: name,host,port,user,password
                    val p = line.split(",").map { it.trim() }
                    if (p.size < 2) {
                        null
                    } else {
                        Server(
                            name = p[0],
                            host = p[1],
                            port = p.getOrNull(2)?.toIntOrNull() ?: 22,
                            user = p.getOrNull(3)?.ifEmpty { null } ?: "root",
