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
    // todo change it

    private const val geoDir = "C:/Users/Leon/Desktop/geo"
    private val dbFile = "$geoDir/GeoLite2-City.mmdb".toFile()
    private val dbCountryFile = "$geoDir/GeoLite2-Country.mmdb".toFile()

    val cityReader: DatabaseReader by lazy {
        DatabaseReader.Builder(dbFile).withCache(CHMCache()).build()
    }
    val countryReader: DatabaseReader by lazy {
        DatabaseReader.Builder(dbCountryFile).withCache(CHMCache()).build()
    }
}

fun String.ipCountryZh() =
    runCatching { countryReader.country(this.toInetAddress()).country.names["zh-CN"] }.getOrElse {
        println("ipCountryZh error ${it.message}")
        "UNKNOWN"
    }

fun InetAddress.ipCountryZh() =
    kotlin
        .runCatching { countryReader.country(this).country.names["zh-CN"] }
        .onFailure { "UNKNOWN" }
        .getOrNull()

fun String.ipCountryEn() =
    kotlin
        .runCatching { countryReader.country(this.toInetAddress()).country.isoCode }
        .onFailure { "UNKNOWN" }
        .getOrNull()

fun InetAddress.ipCountryEn() =
    kotlin
        .runCatching { countryReader.country(this).country.isoCode }
        .onFailure { "UNKNOWN" }
        .getOrNull()

fun String.ipCityZh() =
    kotlin
        .runCatching {
            cityReader.city(this.toInetAddress()).run {
                mostSpecificSubdivision.names["zh-CN"] ?: country.names["zh-CN"]
            }
        }
        .onFailure { "UNKNOWN" }
        .getOrNull()

fun String.ipCityEn() =
    kotlin
        .runCatching {
            cityReader.city(this.toInetAddress()).run {
                mostSpecificSubdivision.names["en"] ?: country.names["en"]
            }
        }
        .onFailure { "UNKNOWN" }
        .getOrNull()
