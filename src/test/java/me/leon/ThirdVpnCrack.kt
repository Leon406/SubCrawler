package me.leon

import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.leon.domain.Quark
import me.leon.domain.Sumurai
import me.leon.support.DISPATCHER
import me.leon.support.b64Decode
import me.leon.support.connect
import me.leon.support.fromJson
import me.leon.support.ping
import me.leon.support.readFromNet
import org.junit.jupiter.api.Test

class ThirdVpnCrack {
    @Test
    fun parseNet() {
        val key = SimpleDateFormat("yyyyMMdd").format(Date()).repeat(4)
        "https://ghproxy.com/https://raw.githubusercontent.com/webdao/v2ray/master/nodes.txt"
            .readFromNet()
            .b64Decode()
            .foldIndexed(StringBuilder()) { index, acc, c ->
                acc.also { acc.append((c.code xor key[index % key.length].code).toChar()) }
            }
            .also { println(it) }
            .split("\n")
            .also { println(it.joinToString("|")) }
    }

    @Test
    fun parseSumaraiVpn() {
        runBlocking {
            "https://server.svipvpn.com/opconf.json"
                .readFromNet()
                .fromJson<Sumurai>()
                .data
                .items
                .flatMap { it.items }
                .mapNotNull { Parser.parse(it.ovpn.b64Decode()) }
                .map { it to async(DISPATCHER) { it.SERVER.connect(it.serverPort, 2000) } }
                .filter { it.second.await() > -1 }
                .forEach { println(it.first.toUri()) }
        }
    }

    val quarkVpnDir = "$ROOT/vpn/quark"

    @Test
    fun parseQuarkVpn() {
        runBlocking {
            File(quarkVpnDir)
                .listFiles()
                .map {
                    String(
                            it.readBytes()
                                .mapIndexed { index, byte ->
                                    if (index % 2 == 0) (byte - 1).toByte() else (byte + 1).toByte()
                                }
                                .toByteArray()
                        )
                        .fromJson<Quark>()
                }
                .flatMap { it.data.map { it.host to it.name } }
                .distinctBy { it.first }
                .also { println("${it.size}  $it") }
                .map { it to async(DISPATCHER) { it.first.ping(2000) } }
                .filter { it.second.await() > -1 }
                .also { println("ok ${it.size} ") }
                .map {
                    SS("aes-256-cfb", "4415934295", it.first.first, "50004")
                        .apply { remark = it.first.second }
                        .toUri()
                }
                .also { println(it.joinToString("\n")) }
        }
    }
}
