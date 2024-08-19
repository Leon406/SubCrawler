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

    @Test
    fun geoParse() {

        val ipAddress = "128.101.101.101"
//        val ipAddress = "104.19.45.161"

        println(ipAddress.ipCountryZh())
//        println(ipAddress.ipCountryEn())
//        println(ipAddress.ipCityZh())
//        println(ipAddress.ipCountryEn())
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
