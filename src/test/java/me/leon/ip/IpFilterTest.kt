package me.leon.ip

import kotlin.system.measureTimeMillis
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.leon.FAIL_IPS
import me.leon.support.*
import org.junit.jupiter.api.Test

class IpFilterTest {

    @Test
    fun reTestFailIps() {
        failIp()
        deleteOkIps()
    }

    private fun failIp() {
        val okIps = mutableListOf<String>()
        val failIps = mutableListOf<String>()
        val total = mutableListOf<String>()

        measureTimeMillis {
            FAIL_IPS
                .readLines()
                .also { println("before ${it.size}") }
                .toHashSet()
                .sorted()
                .also {
                    total.addAll(it)
                    println("after ${it.size}")
                    FAIL_IPS.writeLine()
                    FAIL_IPS.writeLine(it.joinToString("\n"))
                }
                .groupBy { it.contains(':') }
                .also { map ->
                    map[true]
                        .also { println("带端口节点数量 ${it?.size}") }
                        ?.map { it.substringBeforeLast(':') to it }
                        ?.forEach { p ->
                            if (okIps.contains(p.first) || failIps.contains(p.first)) {
                                //                                    println("已存在")
                                return@forEach
                            }
                            if (p.first.ping(1000) > -1) okIps.add(p.first)
                            else println(p.second.also { failIps.add(p.first) })
                        }
                }

            println(failIps)
            println("_______")
            println(okIps)
            total
                .toHashSet()
                .also {
                    println("before ${it.size}")
                    it.removeAll(okIps)
                    it.addAll(failIps)
                }
                .filterNot { it.contains(":") && failIps.contains(it.substringBeforeLast(":")) }
                .sorted()
                .also {
                    FAIL_IPS.writeLine()
                    FAIL_IPS.writeLine(it.joinToString("\n"))
                    println("after ${it.size}")
                }
        }
            .also { println("time $it ms") }
    }

    private fun deleteOkIps() {
        val total = mutableListOf<String>()
        runBlocking {
            FAIL_IPS
                .readLines()
                .also {
                    total.addAll(it)
                    println("before ${it.size}")
                }
                .filterNot { it.contains(":") }
                .map { it to async(DISPATCHER) { it.connect() } }
                .filter { it.second.await() > -1 }
                .forEach {
                    println(it.first)
                    total.remove(it.first)
                }
        }
        println("after ${total.size}")
        FAIL_IPS.writeLine()
        FAIL_IPS.writeLine(total.joinToString("\n"))
    }
}
