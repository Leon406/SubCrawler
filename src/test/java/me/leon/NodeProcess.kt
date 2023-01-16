package me.leon

import org.junit.jupiter.api.Test

class NodeProcess {

    @Test
    fun nodeNationGroup() {
        Parser.parseFromSub(NODE_OK)
            .filter { it.SERVER.contains("p3r.centaur.net") && it.SERVER.ipCountryZh() == "中国" }
            .groupBy { it.SERVER.ipCountryZh() }
            .forEach { (t, u) ->
                println("$t: ${u.size}")
                if (t == "UNKNOWN") println(u.map { it.SERVER })
            }
    }
}
