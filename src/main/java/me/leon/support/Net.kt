package me.leon.support

import java.io.DataOutputStream
import java.io.File
import java.net.*
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.leon.FAIL_IPS

val failIpPorts by lazy { FAIL_IPS.readLines().toHashSet() }
val fails = mutableSetOf<String>()
val passes = mutableSetOf<String>()

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
    } else if (passes.contains("$this:$port")) {
        1
    } else {
        runCatching {
                measureTimeMillis {
                    Socket().connect(InetSocketAddress(this, port), timeout)
                    passes.add("$this:$port")
                }
            }
            .getOrElse {
                exceptionHandler.invoke("$this:$port")
                -1
            }
    }

/** ping 测试 */
fun String.ping(
    timeout: Int = 1000,
    cacheFailed: (ip: String, port: Int) -> Boolean = Nop,
    exceptionHandler: (info: String) -> Unit = {}
) =
    if (!contains(".") || cacheFailed.invoke(this, -1)) {
        println("fast failed")
        -1
    } else if (passes.contains(this)) {
        1
    } else {
        runCatching {
                val start = System.currentTimeMillis()
                val reachable = InetAddress.getByName(this).isReachable(timeout)
                if (reachable) {
                    (System.currentTimeMillis() - start).also { passes.add(this) }
                } else {
                    exceptionHandler.invoke(this)
                    -1
                }
            }
            .getOrElse {
                exceptionHandler.invoke(this)
                -1
            }
    }

fun String.toFile() = File(this)

const val RESPONSE_OK = 200

fun String.post(params: MutableMap<String, String>) =
    runCatching {
            val p =
                params.keys
                    .foldIndexed(StringBuilder()) { index, acc, s ->
                        acc.also {
                            acc.append("${"&".takeUnless { index == 0 }.orEmpty()}$s=${params[s]}")
                        }
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

fun String.readBytesFromNet(
    method: String = "GET",
    timeout: Int = 3000,
    data: String = "",
    headers: MutableMap<String, Any> = mutableMapOf()
) =
    runCatching {
            (URL(this).openConnection() as HttpURLConnection)
                .apply {
                    connectTimeout = timeout
                    readTimeout = timeout
                    setRequestProperty("Content-Type", "zh-CN,zh;q=0.9,en;q=0.8")
                    setRequestProperty("Accept-Language", "application/json; charset=utf-8")
                    setRequestProperty(
                        "user-agent",
                        "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/86.0.4240.198 Safari/537.36"
                    )
                    for ((k, v) in headers) setRequestProperty(k, v.toString())

                    requestMethod = method

                    if (method.equals("post", true)) {
                        val dataBytes = data.toByteArray()
                        if (dataBytes.isNotEmpty()) {
                            addRequestProperty("Content-Length", dataBytes.size.toString())
                        }
                        doOutput = true
                        connect()
                        outputStream.write(dataBytes)
                        outputStream.flush()
                        outputStream.close()
                    }
                }
                .takeIf { it.responseCode == RESPONSE_OK }
                ?.inputStream
                ?.readBytes()
                ?: byteArrayOf()
        }
        .getOrElse {
            println("read bytes err  ")
            byteArrayOf()
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

@OptIn(ExperimentalCoroutinesApi::class) val DISPATCHER = Dispatchers.IO.limitedParallelism(256)
