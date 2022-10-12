package com.fetch.cloudflarebypass

interface Log {
    fun i(tag: String, s: String)
    fun e(tag: String, s: String)
}