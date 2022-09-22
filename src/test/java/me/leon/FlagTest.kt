package me.leon

import me.leon.support.FlagRemover
import me.leon.support.removeFlags
import org.junit.jupiter.api.Test

class FlagTest {
    @Test
    fun flagParse() {

        FlagRemover.remove("Relay_\uD83C\uDDE8\uD83C\uDDF3CN-\uD83C\uDDF8\uD83C\uDDECSG_1927")
            .also { println(it) }
    }

    @Test
    fun pool() {
        Parser.parseFromSub(NODE_OK).map { println("${it.name} ${it.name.removeFlags()}") }
    }
}
