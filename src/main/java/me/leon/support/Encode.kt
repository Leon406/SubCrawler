package me.leon.support

import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

fun String.b64Decode() = String(Base64.getDecoder().decode(this.filter { it.code < 128 }))

fun String.b64SafeDecode() =
    if (this.contains(":")) {
        this
    } else {
        runCatching {
                if (contains("=[^=]+.+".toRegex())) {
                    split("=")
                        .filter { it.isNotEmpty() }
                        .joinToString(System.lineSeparator()) {
                            it.replace("_", "/").replace("-", "+").b64Decode()
                        }
                } else {
                    trim().replace("_", "/").replace("-", "+").b64Decode()
                }
            }
            .getOrElse {
                println("failed: $this  ${it.message}")
                ""
            }
    }

fun String.b64Encode(): String = Base64.getEncoder().encodeToString(toByteArray())

fun String.b64EncodeNoEqual() = Base64.getEncoder().encodeToString(toByteArray()).replace("=", "")

fun String.urlEncode() = URLEncoder.encode(this).orEmpty()

fun String.urlDecode() = URLDecoder.decode(this).orEmpty()
