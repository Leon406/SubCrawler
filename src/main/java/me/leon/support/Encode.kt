package me.leon.support

import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Base64

fun String.b64Decode() = String(Base64.getDecoder().decode(this))

fun String.b64SafeDecode() =
    if (this.contains(":")) this
    else {
        runCatching {
                String(Base64.getDecoder().decode(this.trim().replace("_", "/").replace("-", "+")))
            }
            .getOrElse {
                println("failed:  ${it.message}")
                ""
            }
    }

fun String.b64Encode(): String = Base64.getEncoder().encodeToString(this.toByteArray())

fun String.b64EncodeNoEqual() =
    Base64.getEncoder().encodeToString(this.toByteArray()).replace("=", "")

fun String.urlEncode() = URLEncoder.encode(this).orEmpty()

fun String.urlDecode() = URLDecoder.decode(this).orEmpty()
