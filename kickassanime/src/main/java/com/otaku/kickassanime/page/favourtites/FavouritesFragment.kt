package com.otaku.kickassanime.page.favourtites

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.paging.map
import com.otaku.fetch.base.ui.setOnClick
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.ItemFavoriteBinding
import com.otaku.kickassanime.db.models.AnimeFavorite
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.page.frontpage.list.ListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class FavouritesFragment : ListFragment<ItemFavoriteBinding>() {

    private val viewModel: FavouritesViewModel by viewModels()

    override fun getList(): Flow<PagingData<ITileData>> {
        return viewModel.favourites.map { it.map { fav -> fav } }
    }

    override fun getListTag() = "Favourites"

    private fun onItemClick(item: ITileData) {
        val fav = item as? AnimeFavorite ?: return
        FavouritesFragmentDirections.actionFavouritesFragmentToAnimeActivity(
            AnimeEntity(
                animeSlugId = fav.animeSlugId,
                animeSlug = fav.animeSlug
            )
        )
    }

    override val layoutId: Int
        get() = R.layout.item_favorite

    override val onBind: (ItemFavoriteBinding, ITileData) -> Unit =
        { itemFavoriteBinding: ItemFavoriteBinding, iTileData: ITileData ->
            if (iTileData is AnimeFavorite) {
                itemFavoriteBinding.fav = iTileData
                itemFavoriteBinding.root.setOnClick {
                    val actionFavouritesFragmentToAnimeActivity =
                        FavouritesFragmentDirections.actionFavouritesFragmentToAnimeActivity(
                            AnimeEntity(
                                animeSlugId = iTileData.animeSlugId,
                                animeSlug = iTileData.animeSlug,
                            )
                        )
                    findNavController().navigate(actionFavouritesFragmentToAnimeActivity)
                }
                itemFavoriteBinding.favorite.setOnClick {
                    viewModel.removeFavourite(iTileData.animeSlugId)
                }
                itemFavoriteBinding.root.setOnClick {
                    onItemClick(iTileData)
                }
            }
        }
}