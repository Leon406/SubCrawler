package me.leon.support

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.representer.Representer

inline fun <reified T> String.parseYaml() = (Yaml(Constructor(T::class.java, LoaderOptions().apply {
    codePointLimit = Int.MAX_VALUE
}), Representer(DumperOptions()).apply { propertyUtils.isSkipMissingProperties = true }).load(fixYaml()) as T)

fun String.fixYaml() =
    replace("!<[^>]+>".toRegex(), "")
        .replace("  password: \n", "  password: xxxxx\n")
        .replace("server: $*@", "server: ")
