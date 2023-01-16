package me.leon.support

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

inline fun <reified T> String.parseYaml() = (Yaml(Constructor(T::class.java)).load(fixYaml()) as T)

fun String.fixYaml() =
    replace("!<[^>]+>".toRegex(), "")
        .replace("  password: \n", "  password: xxxxx\n")
        .replace("server: $*@", "server: ")
        .replace(
            "\"?(?:UpdateDay|up|down|recv_window|recv_window_conn|PFirstFoundDay|minimum|maximum|_?index|average|Rank|success_rate)\"?:\\s*[-\\dT:.]+,?".toRegex(),
            ""
        )
        .replace("udp:true", "udp: true")
