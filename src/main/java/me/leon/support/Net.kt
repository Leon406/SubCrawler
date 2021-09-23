package me.leon.support

import java.io.DataOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.asCoroutineDispatcher
import me.leon.FAIL_IPS

val failIpPorts by lazy { FAIL_IPS.readLines().toHashSet() }
val fails = mutableSetOf<String>()

/** ip + port 测试 */
val Nop = { _: String, _: Int -> false }

fun String.connect(
    port: Int = 80,
    timeout: Int = 1000,
    cache: (ip: String, port: Int) -> Boolean = Nop,
    exceptionHandler: (info: String) -> Unit = {}
) =
    if (!contains(".") || port < 0 || cache.invoke(this, port)) {
        //        println("quick fail from cache")
        -1
    } else {
        runCatching {
            measureTimeMillis { Socket().connect(InetSocketAddress(this, port), timeout) }
        }
            .getOrElse {
                exceptionHandler.invoke("$this:$port")
                -1
            }
    }

/** ping 测试 */
fun String.ping(
    timeout: Int = 1000,
    cache: (ip: String, port: Int) -> Boolean = Nop,
    exceptionHandler: (info: String) -> Unit = {}
) =
    if (!contains(".") || cache.invoke(this, -1)) {
        println("fast failed")
        -1
    } else
        runCatching {
            val start = System.currentTimeMillis()
            val reachable = InetAddress.getByName(this).isReachable(timeout)
            if (reachable) (System.currentTimeMillis() - start)
            else {
                println("$this unreachable")
                exceptionHandler.invoke(this)
                -1
            }
        }
            .getOrElse {
                println("ping err $it  $this")
                exceptionHandler.invoke(this)
                -1
            }

fun String.toFile() = File(this)

const val RESPONSE_OK = 200

fun String.post(params: MutableMap<String, String>) =
    runCatching {
        val p =
            params
                .keys
                .foldIndexed(StringBuilder()) { index, acc, s ->
                    acc.also { acc.append("${"&".takeUnless { index == 0 } ?: ""}$s=${params[s]}") }
                }
                .toString()
        String(
            URL(this)
                .openConnection()
                .safeAs<HttpURLConnection>()
                ?.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    setRequestProperty("Referer", "https://pc.woozooo.com/mydisk.php")
                    setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9")
                    setRequestProperty("Content-Length", "${p.toByteArray().size}")
                    setRequestProperty(
                        "user-agent",
                        "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/86.0.4240.198 Safari/537.36"
                    )
                    useCaches = false
                    doInput = true
                    doOutput = true

                    DataOutputStream(outputStream).use { it.writeBytes(p) }
                }
                ?.takeIf {
                    //            println("$this __ ${it.responseCode}")
                    it.responseCode == RESPONSE_OK
                }
                ?.inputStream
                ?.readBytes()
                ?: "".toByteArray()
        )
    }
        .getOrElse {
            println("$this read err ${it.message}")
            ""
        }

fun String.quickConnect(port: Int = 80, timeout: Int = 1000) =
    this.connect(
        port,
        timeout,
        { ip, p ->
            failIpPorts.contains(ip) || fails.contains("$ip:$p") || failIpPorts.contains("$ip:$p")
        }
    ) {
        //    println("error $it")
        fails.add(it)
        FAIL_IPS.writeLine(it)
    }

fun String.quickPing(timeout: Int = 1000) =
    this.ping(timeout, { ip, _ -> failIpPorts.contains(ip) || fails.contains(ip) }) {
        println("error $it")
        fails.add(it)
        FAIL_IPS.writeLine(it)
    }

val DISPATCHER =
    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)
        .asCoroutineDispatcher()
