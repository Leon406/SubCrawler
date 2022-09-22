package me.leon.support

import me.leon.ROOT

object FlagRemover {
    private val flagMaps = linkedMapOf<String, String>()
    private val flagReg by lazy {
        "$ROOT/flags.txt"
            .readLines()
            .map {
                with(it.split("|")) {
                    flagMaps[this[0]] = this[2]
                    flagMaps[this[1]] = this[2]
                    Triple(this[0], this[1], this[2])
                }
            }
            .joinToString("|") { "(${it.first}) *(?:${it.second})?" }
            //            .also { println(it) }
            .toRegex()
    }

    fun remove(s: String): String {
        var tmp = s
        flagReg
            .findAll(s)
            .map {
                it.groupValues
                    .filterIndexed { i, item -> i != 0 && item.isNotEmpty() }
                    .map { item ->
                        //                    println("$item ${flagMaps[item]}")
                        flagMaps[item]?.run {
                            tmp =
                                flagReg.replaceFirst(
                                    tmp,
                                    this.takeUnless { tmp.contains(this) }.orEmpty()
                                )
                        }
                    }
            }
            .lastOrNull()
        return tmp.replace("美国离岛美国|美国离岛".toRegex(), "美国")
    }
}

fun String.removeFlags() = FlagRemover.remove(this)
