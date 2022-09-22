package me.leon

import com.maxmind.db.CHMCache
import com.maxmind.geoip2.DatabaseReader
import java.net.InetAddress
import me.leon.GeoParser.cityReader
import me.leon.GeoParser.countryReader
import me.leon.support.toFile
import me.leon.support.toInetAddress

object GeoParser {
    // register at https://www.maxmind.com/, and download your file,you also can download from
    // https://leon.lanzoui.com/i4XoWph8yaj
    // todo change it to your own file
    private const val geoDir = "C:/Users/Leon/Desktop/geo"
    private val dbFile = "$geoDir/GeoLite2-City.mmdb".toFile()
    private val dbCountryFile = javaClass.getResource("/GeoLite2-Country.mmdb").file.toFile()

    val cityReader: DatabaseReader by lazy {
        DatabaseReader.Builder(dbFile).withCache(CHMCache()).build()
    }
    val countryReader: DatabaseReader by lazy {
        DatabaseReader.Builder(dbCountryFile).withCache(CHMCache()).build()
    }
}

private const val CN = "zh-CN"

fun String.ipCountryZh() =
    runCatching { countryReader.country(this.toInetAddress()).country.names[CN] }
        .getOrElse {
            println("ipCountryZh error ${it.message}")
            "未知"
        }

fun InetAddress.ipCountryZh() =
    runCatching { countryReader.country(this).country.names[CN] }.getOrDefault("未知")

fun Sub.ipCountryZh(): String =
    runCatching { countryReader.country(SERVER.toInetAddress()).country.names[CN] }
        .getOrDefault("未知")
        ?: "未知"

fun String.ipCountryEn(): String =
    runCatching { countryReader.country(this.toInetAddress()).country.isoCode }
        .getOrDefault("UNKNOWN")

fun InetAddress.ipCountryEn(): String =
    runCatching { countryReader.country(this).country.isoCode }.getOrDefault("UNKNOWN")

fun String.ipCityZh() =
    runCatching {
            cityReader.city(this.toInetAddress()).run {
                mostSpecificSubdivision.names[CN] ?: country.names[CN]
            }
        }
        .getOrDefault("未知")

fun String.ipCityEn() =
    runCatching {
            cityReader.city(this.toInetAddress()).run {
                mostSpecificSubdivision.names["en"] ?: country.names["en"]
            }
        }
        .getOrDefault("未知")
