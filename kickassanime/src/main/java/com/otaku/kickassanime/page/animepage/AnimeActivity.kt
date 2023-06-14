package com.otaku.kickassanime.page.animepage

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.PagingData
import com.otaku.fetch.base.livedata.State
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.AnimeLanguageEntity
import com.otaku.kickassanime.page.episodepage.EpisodeActivity
import com.otaku.kickassanime.page.episodepage.EpisodeActivityArgs
import com.otaku.kickassanime.ui.composables.FavoriteButton
import com.otaku.kickassanime.ui.theme.KickassAnimeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@AndroidEntryPoint
class AnimeActivity : AppCompatActivity() {
    private val args: AnimeActivityArgs? by lazy {
        intent.extras?.let {
            AnimeActivityArgs.fromBundle(it)
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            args?.anime?.let { arg ->
                val animeViewModel: AnimeViewModel = viewModel()
                LaunchedEffect(Unit, block = {
                    animeViewModel.fetchLanguages(arg.animeSlug)
                })
                val currentContext = LocalContext.current
                AnimeMainScreen(
                    arg.name ?: "",
                    arg.favourite,
                    animeViewModel.animeLanguageState,
                    {
                        animeViewModel.setFavourite(arg.animeSlug, it)
                    },
                    {
                        animeViewModel.getAnime(arg.animeSlug)
                    },
                    {
                        animeViewModel.getLanguages(arg.animeSlug)
                    },
                    { language ->
                        animeViewModel.getEpisodeList(arg.animeSlug, language)
                    }
                ) { episodeTile ->
                    currentContext.startActivity(
                        Intent(
                            currentContext,
                            EpisodeActivity::class.java
                        ).apply {
                            putExtras(
                                EpisodeActivityArgs(
                                    animeSlug = arg.animeSlug,
                                    episodeSlug = episodeTile.slug ?: return@AnimeMainScreen,
                                    title = arg.name ?: return@AnimeMainScreen
                                ).toBundle()
                            )
                            flags =
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        })
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AnimeMainScreen(
        animeName: String = "",
        isFavouriteValue: Boolean = false,
        animeLanguageState: State,
        applyFavourite: (Boolean) -> Unit = {},
        getAnime: () -> Flow<AnimeEntity?> = {
            flow { }
        },
        getLanguages: () -> Flow<List<AnimeLanguageEntity>> = {
            flow { }
        },
        getEpisodeList: (String?) -> Flow<PagingData<EpisodeTile>> = {
            flow { }
        },
        onEpisodeClick: (EpisodeTile) -> Unit = {}
    ) {
        val (isFavourite, setFavorite) = remember { mutableStateOf(isFavouriteValue) }
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        KickassAnimeTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    LargeTopAppBar(
                        title = { Text(text = animeName, fontWeight = FontWeight.W800) },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    tint = contentColorFor(backgroundColor = MaterialTheme.colorScheme.background),
                                    contentDescription = "Localized description"
                                )
                            }
                        },
                        actions = {
                            FavoriteButton(onClick = {
                                applyFavourite(!isFavourite)
                                setFavorite(!isFavourite)
                            }, isChecked = isFavourite)
                        },
                        scrollBehavior = scrollBehavior
                    )
                },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                ) {
                    AnimeScreen(
                        Modifier.fillMaxSize(),
                        animeLanguageState,
                        getAnime,
                        getLanguages,
                        getEpisodeList,
                        onEpisodeClick
                    )
                }
            }
        }
    }

    @Composable
    @Preview
    fun AnimeDummyScreen() {
        KickassAnimeTheme {
            AnimeMainScreen(
                animeName = "432",
                true,
                State.SUCCESS(),
                getAnime = {
                    return@AnimeMainScreen flow {
                        emit(AnimeEntity("", "jsaklj", "hodfhjaksdh"))
                    }
                }
            )
        }
    }


    class AnimeActivityArgs(var anime: AnimeEntity) {
        companion object {
            private const val ARG_ANIME = "anime_args"

            @Suppress("deprecation")
            fun fromBundle(bundle: Bundle): AnimeActivityArgs? {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable(ARG_ANIME, AnimeEntity::class.java)
                        ?.let { AnimeActivityArgs(it) }
                } else {
                    bundle.getParcelable<AnimeEntity>(ARG_ANIME)?.let { AnimeActivityArgs(it) }
                }
            }

            fun toBundle(anime: AnimeEntity): Bundle {
                return bundleOf(ARG_ANIME to anime)
            }
        }
    }

    companion object {
        fun newInstance(activity: Activity, anime: AnimeEntity): Intent {
            return Intent(activity, AnimeActivity::class.java).apply {
                putExtras(AnimeActivityArgs.toBundle(anime))
            }
        }

    }

}
