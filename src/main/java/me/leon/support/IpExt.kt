package me.leon.support

import me.leon.IP_SCORE
import org.jsoup.Jsoup
import java.net.InetAddress
import kotlin.math.pow

@Throws
fun String.toInetAddress(): InetAddress = InetAddress.getByName(this)


val cfCidrs =
    setOf(
        "103.21.244.0/22",
        "103.22.200.0/22",
        "103.31.4.0/22",
        "104.16.0.0/13",
        "104.24.0.0/14",
        "108.162.192.0/18",
        "131.0.72.0/22",
        "141.101.64.0/18",
        "162.158.0.0/15",
        "172.64.0.0/13",
        "173.245.48.0/20",
        "188.114.96.0/20",
        "190.93.240.0/20",
        "197.234.240.0/22",
        "198.41.128.0/17"
    )
        .map { it.cidrRange() }

fun String.ip2Uint() = split(".").fold(0U) { acc, s -> acc * 256U + s.toUInt() }

fun UInt.toIp() = "${shr(24)}.${shr(16) and 0xFFU}.${shr(8) and 0xFFU}.${this and 0xFFU}"

fun String.ip2Binary() =
    split(".")
        .fold(StringBuilder()) { acc, s -> acc.append(s.toUInt().toString(2).padStart(8, '0')) }
        .toString()

fun Int.ipMaskBinary() = "1".repeat(this) + "0".repeat(32 - this)

fun Int.ipMask() = ipMaskBinary().binary2Ip()

fun String.binary2Ip() = stripAllSpace().chunked(8).joinToString(".") { it.toUInt(2).toString() }

fun String.cidrRange(): UIntRange {
    val (ipStr, cidrStr) = split("/").takeIf { it.size > 1 } ?: listOf(this, "24")
    val cidr = cidrStr.takeIf { it.isNotEmpty() }?.toInt() ?: 24
    val ip = ipStr.ip2Uint()
    val sub = 32 - cidr
    val count = 2.0.pow(sub).toInt()
    val mask = cidr.ipMask().ip2Uint()
    val net = mask and ip
    return (net + 1U)..(net + (count - 2).toUInt())
}

fun String.ipCloudFlare() = cfCidrs.any { it.contains(ip2Uint()) }

fun String.stripAllSpace() = replace("\\s+".toRegex(), "")

val map = IP_SCORE.readLines().filter { it.isNotEmpty() }.associate { with(it.split("\t")) { first() to last().toInt() } }.toMutableMap()
        .also { println("ip score size ${it.size}")  }

fun String.ipScore() =
    map[this] ?: (Jsoup.connect("https://scamalytics.com/ip/$this")
        .get()
        .selectFirst(".score")
        ?.text()?.substringAfter("Fraud Score: ")?.toIntOrNull() ?: 0)
        .also {
            map[this] = it
            IP_SCORE.writeLine("$this\t$it")
        }

val Int.lvl
    get() =
        when {
            this < 40 -> "L$this"
            this < 70 -> "M$this"
            this < 100 -> "H$this"
            else -> "H"
        }