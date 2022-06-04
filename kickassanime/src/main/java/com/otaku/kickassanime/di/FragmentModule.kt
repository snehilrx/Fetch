package com.otaku.kickassanime.di

import com.otaku.kickassanime.page.adapters.ItemListAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped


@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @FragmentScoped
    @Provides
    fun itemList(): ItemListAdapter = ItemListAdapter()
}