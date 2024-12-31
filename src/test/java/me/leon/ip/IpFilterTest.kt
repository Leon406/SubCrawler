package me.leon.ip

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.leon.*
import me.leon.support.*
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class IpFilterTest {

    @Test
    fun reTestFailIps() {
        repeat(5) {
            failIp()
            removeOkPorts()
        }
    }

    @Test
    fun failIp() {
        val okIps = mutableSetOf<String>()
        val failIps = mutableSetOf<String>()
        val failPorts = mutableSetOf<String>()
        val total = mutableSetOf<String>()

        runBlocking {
            measureTimeMillis {
                FAIL_IPS.readLines()
                    .also { println("before ${it.size}") }
                    .sorted()
                    .also {
                        total.addAll(it)
                        println("after deduplicate and sort ${total.size}")
                        FAIL_IPS.writeLine()
                        FAIL_IPS.writeLine(total.joinToString("\n"))
                    }
                    .map { it to async(DISPATCHER) { it.substringBeforeLast(':').ping(1000) } }
                    .map { it.second.await() to it.first }
                    .forEach {
                        if (it.first > -1) {
                            okIps.add(it.second.substringBeforeLast(":"))
                            println("reAlive ip ${it.second}")
                        } else {
                            failIps.add(it.second.substringBeforeLast(":"))
                            if (it.second.contains(":")) failPorts.add(it.second)
                        }
                    }

                println(failIps)
                println(failPorts)
                println("_______")
                println(okIps)
                total
                    .also {
                        println("before ${it.size}")
                        it.removeAll(okIps)
                        it.removeAll(failPorts)
                        it.addAll(failIps)
                    }
                    .filterNot {
                        it.contains(":") && failIps.contains(it.substringBeforeLast(":"))
                    }
                    .sorted()
                    .also {
                        FAIL_IPS.writeLine()
                        FAIL_IPS.writeLine(it.joinToString("\n"))
                        println("after ${it.size}")
                    }
            }
                .also { println("time $it ms") }
        }
    }

    @Test
    fun removeOkPorts() {
        val total = mutableSetOf<String>()
        runBlocking {
            FAIL_IPS.readLines()
                .also {
                    total.addAll(it)
                    println("before ${total.size}")
                }
                .filter { it.contains(":") && !it.contains("/") && !it.contains(" ") }
                .map {
                    it to
                            async(DISPATCHER) {
                                runCatching {
                                    it.substringBeforeLast(":").connect(it.substringAfterLast(":").toInt())
                                }.getOrElse { -1 }
                            }
                }
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

    @Test
    fun checkAbuseIp() {
        Parser.parseFromSub("C:\\Users\\Leon\\Desktop\\local.yaml").map { it.SERVER }
            .distinct()
            .also { println(it.size) }
            .filterNot { it.contains("[a-zA-Z]".toRegex()) }
            .also { println(it) }
            .forEach {
                println("$it ${it.ipScore().lvl}")
            }
    }

}

