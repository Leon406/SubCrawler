package me.leon.support

import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

private const val DEFAULT_READ_TIME_OUT = 30_000
private const val DEFAULT_CONNECT_TIME_OUT = 30_000

fun String.httpRequest(timeout:Int = DEFAULT_READ_TIME_OUT) = (URL(this).openConnection().apply {
    //                setRequestProperty("Referer",
    // "https://pc.woozooo.com/mydisk.php")
    connectTimeout = DEFAULT_CONNECT_TIME_OUT
    readTimeout = timeout
    setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
    setRequestProperty(
        "user-agent",
        "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/86.0.4240.198 Safari/537.36 Clash"
    )
} as HttpURLConnection)

fun String.readFromNet() =
    runCatching {
        String(
            (URL(this).openConnection().apply {
                //                setRequestProperty("Referer",
                // "https://pc.woozooo.com/mydisk.php")
                connectTimeout = DEFAULT_CONNECT_TIME_OUT
                readTimeout = DEFAULT_READ_TIME_OUT
                setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                setRequestProperty(
                    "user-agent",
                    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/130.0.0.0 Safari/537.36 Edg/130.0.0.0"
                )
            } as HttpURLConnection)
                .takeIf {
                    //            println("$this __ ${it.responseCode}")
                    it.responseCode == RESPONSE_OK
                }
                ?.inputStream
                ?.readBytes()
                ?: "".toByteArray()
        )
    }
        .getOrElse {
            println("read err ${it.message}")
            ""
        }

fun String.queryParamMap() =
    "(\\w+)=([^&]*)".toRegex().findAll(this).fold(mutableMapOf<String, String>()) { acc, matchResult
        ->
        acc.apply { acc[matchResult.groupValues[1]] = matchResult.groupValues[2] }
    }

fun String.queryParamMapB64() =
    "(\\w+)=([^&]*)".toRegex().findAll(this).fold(mutableMapOf<String, String>()) { acc, matchResult
        ->
        acc.apply {
            acc[matchResult.groupValues[1]] =
                matchResult.groupValues[2].urlDecode().replace(" ", "+").b64SafeDecode()
        }
    }

fun Int.slice(group: Int): MutableList<IntRange> {
    val slice = kotlin.math.ceil(this.toDouble() / group.toDouble()).toInt()
    return (0 until group).foldIndexed(mutableListOf()) { index, acc, i ->
        acc.apply {
            acc.add(
                slice * index until ((slice * (i + 1)).takeIf { group - 1 != index } ?: this@slice)
            )
        }
    }
}

fun <T> Any?.safeAs(): T? = this as? T?

fun timeStamp(timeZone: String = "Asia/Shanghai"): String {
    val instance = Calendar.getInstance()
    TimeZone.setDefault(TimeZone.getTimeZone(timeZone))
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(instance.time)
}
