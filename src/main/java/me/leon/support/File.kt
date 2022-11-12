package me.leon.support

import java.io.File
import java.nio.charset.Charset

fun String.readText(charset: Charset = Charsets.UTF_8) =
    File(this).canonicalFile.takeIf { it.exists() }?.readText(charset).orEmpty()

fun String.writeLine(txt: String = "", isAppend: Boolean = true): Unit =
    if (txt.isEmpty() || !isAppend) {
        File(this).also { if (!it.parentFile.exists()) it.parentFile.mkdirs() }.writeText(txt)
    } else {
        File(this).appendText("$txt${System.lineSeparator()}")
    }

fun String.readLines() = File(this).takeIf { it.exists() }?.readLines() ?: mutableListOf()
