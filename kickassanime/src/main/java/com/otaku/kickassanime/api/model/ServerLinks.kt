package com.otaku.kickassanime.api.model

class ServerLinks {
    val serverName: String
    val link: String

    constructor(serverName: String, link: String) {
        this.serverName = serverName
        this.link = link
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServerLinks

        if (link != other.link) return false

        return true
    }

    override fun hashCode(): Int {
        return link.hashCode()
    }

}