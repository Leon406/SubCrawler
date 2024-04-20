package me.leon

import kotlinx.coroutines.*
import me.leon.support.*
import org.junit.jupiter.api.Test

class ConnectTest {

    @Test
    fun connect() {
        println("www.baidu.com".connect())
        println("www.baidu.com".ping())
        println("www.baidu.com".connect(443))
    }

    @Test
    fun poolTest() {
        NODE_OK.writeLine()
        runBlocking {
            Parser.parseFromSub(POOL)
                .map { it to async(DISPATCHER) { it.SERVER.quickConnect(it.serverPort, 2000) } }
                .filter { it.second.await() > -1 }
                .forEach {
                    println(it.first.info() + ":" + it.second)
                    NODE_OK.writeLine(it.first.toUri())
                }
        }
    }

    @Test
    fun poolPingTest() {
        runBlocking {
            Parser.parseFromSub(POOL)
                .map { it to async(DISPATCHER) { it.SERVER.quickPing(2000) } }
                .filter { it.second.await() > -1 }
                .also { println(it.size) }
                .forEach { println(it.first.info() + ":" + it.second) }
        }
    }

    @Test
    fun url404() {
        runBlocking {
            ("$ROOT/pool/sublists".readLines() + "$ROOT/pool/subs".readLines())
                .filterNot { it.startsWith("#") }
                .map {
                    async(DISPATCHER) {
                        it to runCatching { it.httpRequest(10000).responseCode == 404 }.getOrDefault(false)
                    }
                }
                .awaitAll()
                .filter { it.second }
                .forEach {
                    println(it)
                }
        }
    }
}
