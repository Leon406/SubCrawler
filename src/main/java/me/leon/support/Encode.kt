package me.leon.support

import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Base64

fun String.b64Decode() = String(Base64.getDecoder().decode(this))

fun String.b64SafeDecode() =
    if (this.contains(":")) {
        this
    } else {
        runCatching {
            if (contains("=[^=]+.+".toRegex())) {
                split("=").filter { it.isNotEmpty() }
                    .joinToString(System.lineSeparator()) { it.replace("_", "/").replace("-", "+").base64Decode() }
            } else {
                trim().replace("_", "/").replace("-", "+")
                    .base64Decode()
            }
        }
            .getOrElse {
//                it.printStackTrace()
                println("failed: $this  ${it.message}")
                ""
            }
    }

fun String.base64Decode() = String(Base64.getDecoder().decode(this))

fun String.b64Encode(): String = Base64.getEncoder().encodeToString(this.toByteArray())

fun String.b64EncodeNoEqual() =
    Base64.getEncoder().encodeToString(this.toByteArray()).replace("=", "")

fun String.urlEncode() = URLEncoder.encode(this).orEmpty()

fun String.urlDecode() = URLDecoder.decode(this).orEmpty()
