package me.leon

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.leon.support.*
import org.junit.jupiter.api.Test

class LocalFileSubTest {
    @Test
    fun readLocal() {
        Parser.parseFromSub("$ROOT/V2RayN.txt")
            .joinToString("|") { it.toUri() }
            .also { println(it) }
    }

    @Test
    fun readLocal2() {
        Parser.parseFromSub("$ROOT/subs.txt").joinToString("|") { it.toUri() }.also { println(it) }
    }

    @Test
    fun readLocalDir() {
        runBlocking {
            "C:\\Users\\Leon\\Downloads\\Telegram Desktop"
                .toFile()
                .listFiles()
                .map { Parser.parseFromSub(it.absolutePath) }
                .flatten()
                .distinct()
                .map { it to async(DISPATCHER) { it.SERVER.quickConnect(it.serverPort, 2000) } }
                .filter { it.second.await() > -1 }
                .map { it.first }
                .also { println(it.size) }
                .also { println(it.joinToString("\n") { it.toUri() }) }
        }
    }

    @Test
    fun readLocal4() {
        Parser.parseFromSub(NODE_OK)
            .filterIsInstance<V2ray>()
            .filter { it.net == "grpc" }
            .filterNot { it.methodUnSupported().apply { if (this) println("____$it") } }
            .joinToString("\n") { it.info() + "${it.tls} ${it.path}" }
            .also { println(it) }
    }

    @Test
    fun readLocalSSR() {
        Parser.parseFromSub(NODE_SSR)
            .also { println(it.size) }
            .filterNot { it.methodUnSupported() }
            .filterIsInstance<SSR>()
            //            .joinToString("\n") { it.name }
            .also { println(it.map { it.protocol }.groupBy { it }.keys) }
    }

    @Test
    fun parseUri() {
        val uri =
            "trojan://413f2e36-0038-48e4-963a-a38c0007ef24@us-sp.okzdns.com:50001/#S2%E7%BE%8E%E5%9B%BD%7CNetFlix%7C04"
        Parser.parse(uri).also { println(it) }
    }
}
