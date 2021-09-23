package me.leon.ip

import me.leon.FAIL_IPS
import me.leon.GeoParser.cityReader
import me.leon.ipCityZh
import me.leon.ipCountryEn
import me.leon.ipCountryZh
import me.leon.support.readLines
import me.leon.support.toInetAddress
import org.junit.jupiter.api.Test

class GeoTest {

    fun geoParse() {

        val ipAddress = "128.101.101.101"

        val response = cityReader.city(ipAddress.toInetAddress())
        println(response)

        response.country.run {
            println("country isoCode: $isoCode name: $name name-zh: ${names["zh-CN"]}")
        }
        response.mostSpecificSubdivision.run {
            println("subdivision isoCode: $isoCode name: $name name-zh: ${names["zh-CN"]}")
        }
        response.run { println("city: $city , postal: $postal  location: $location") }

        println(ipAddress.ipCountryZh())
        println(ipAddress.ipCountryEn())
        println(ipAddress.ipCityZh())
        println(ipAddress.ipCountryEn())
    }

    @Test
    fun ip_reader() {
        FAIL_IPS.readLines().forEach {
            """^(\d+(?:.\d+){3})(:\d+)?$""".toRegex().matchEntire(it)?.run {
                println(this.groupValues[1] to this.groupValues[1].toInetAddress().ipCountryZh())
            }
            //            println(reader2.country(InetAddress.getByName(it)).country.names["zh-CN"])
        }
    }
}
