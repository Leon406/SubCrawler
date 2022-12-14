package me.leon

import org.junit.jupiter.api.Test

class NetworkSubTest {
    @Test
    fun subParse() {
        // https://ghproxy.com/https://raw.githubusercontent.com/samjoeyang/subscribe/main/fly
        val e =
            "https://github.moeyy.xyz/https://raw.githubusercontent.com/visiti/clash-node/main/clash-node.yaml"
        //        runBlocking {
        //            Parser.parseFromSub(e)
        //                .map { it to async(DISPATCHER) { it.SERVER.quickConnect(it.serverPort,
        // 1000) } }
        //                .filter { it.second.await() > -1 }
        //                .also { println(it.size) }
        //                .forEach { println(it.first.info() + ":" + it.second) }
        //        }

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
