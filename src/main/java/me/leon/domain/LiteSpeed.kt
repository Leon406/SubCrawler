package me.leon.domain

data class LiteSpeed(val id: Int, val info: String) {

    var servers: List<Server>? = null

    private var speed: String = ""
    private var ping: Int = -1

    fun ping(): Int? {
        return if (info == GOT_PING && ping > 0) {
            ping
        } else {
            null
        }
    }

    fun isEnd() = info == END_ONE

    fun speed(): String? {
        return if (info == GOT_SPEED) {
            speed
        } else {
            null
        }
    }

    data class Server(
        val id: Int,
        val link: String,
    )

    companion object {
        const val GOT_SERVERS = "gotservers"
        const val GOT_PING = "gotping"
        const val GOT_SPEED = "gotspeed"
        const val END_ONE = "endone"
        const val NA = "N/A"
    }
}

data class LiteSpeedConfig(
    val subscription: String = "",
    val group: String = "Default",
    val speedtestMode: String = "pingonly",
    val pingMethod: String = "googleping",
    val sortMethod: String = "pingonly",
    val concurrency: Int = 128,
    val testMode: Int = 2,
    val timeout: Int = 5,
    val fontSize: Int = 24,
    val generatePicMode: Int = 2,
    val unique: Boolean = true,
    val language: String = "en",
    val theme: String = "rainbow",
)
