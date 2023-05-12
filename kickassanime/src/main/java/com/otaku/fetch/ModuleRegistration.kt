package com.otaku.fetch

import android.content.Context
import com.otaku.fetch.ModuleRegistry.registerModule
import com.otaku.kickassanime.KickassAppModule
import com.otaku.kickassanime.R
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class ModuleRegistration0 : ModuleLoaders {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ModuleRequirements {
        fun kickassDb() : KickassAnimeDb
        fun kickassAnimeService() : KickassAnimeService
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun  load(applicationContext: Context) {
        val moduleRequirements = EntryPointAccessors.fromApplication<ModuleRequirements>(applicationContext)
        registerModule(
            "Kickass Anime",
            R.drawable.kaa,
            KickassAppModule(moduleRequirements.kickassAnimeService(), moduleRequirements.kickassDb())
        )
    }
}