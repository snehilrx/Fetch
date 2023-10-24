package com.otaku.kickassanime.api.model

import androidx.media3.exoplayer.offline.Download

sealed class ServerLinks(open val serverName: String) {

    class OnlineServerLink(override val serverName: String, val link: String) :
        ServerLinks(serverName) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is OnlineServerLink) return false

            if (link != other.link) return false

            return true
        }

        override fun hashCode(): Int {
            return link.hashCode()
        }
    }

    class OfflineServerLink(override val serverName: String, val download: Download) :
        ServerLinks(serverName) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is OfflineServerLink) return false

            if (serverName != other.serverName) return false

            return true
        }

        override fun hashCode(): Int {
            return serverName.hashCode()
        }
    }


}