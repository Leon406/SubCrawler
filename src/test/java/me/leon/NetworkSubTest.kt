package me.leon

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.leon.support.DISPATCHER
import me.leon.support.quickConnect
import org.junit.jupiter.api.Test

class NetworkSubTest {
    @Test
    fun subParse() {
        val e = "https://update.glados-config.org/clash/82245/e52c7e5/53325/glados_new.yaml"
        runBlocking {
            Parser.parseFromSub(e)
                .map { it to async(DISPATCHER) { it.SERVER.quickConnect(it.serverPort, 1000) } }
                .filter { it.second.await() > -1 }
                .also { println(it.size) }
                .forEach { println(it.first.info() + ":" + it.second) }
        }

        listOf(
                e,
            )
            .forEach {
                kotlin
                    .runCatching {
                        Parser.parseFromSub(it)
                            .also { println(it.size) }
                            .joinToString(
                                //                        "|",
                                "\r\n",
                                transform = Sub::toUri
                            )
                            .also {
                                println("___________")
                                println(it)
                            }
                    }
                    .onFailure { it.printStackTrace() }
            }
    }

    @Test
    fun sub() {
        val l1 = Parser.parseFromSub("https://etproxypool.ga/clash/proxies")
        val l2 = Parser.parseFromSub("https://suo.yt/v9UsfNr")
        val combine = l1 + l2
        val l1Only = combine - l2
        val l2Only = combine - l1
        val share = l1 - l1Only
        println("共享 ${share.size}")
        println("l1 ${l1.size} 独有 ${l1Only.size}")
        println("l2 ${l2.size} 独有 ${l2Only.size}")
    }
}
