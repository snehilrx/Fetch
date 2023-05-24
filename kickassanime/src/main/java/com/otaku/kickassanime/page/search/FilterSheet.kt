package com.otaku.kickassanime.page.search

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.maxkeppeler.sheets.core.Sheet
import com.otaku.fetch.base.utils.UiUtils.toPxInt
import com.otaku.kickassanime.R
import com.otaku.kickassanime.api.model.Filters
import kotlinx.coroutines.launch

class FilterSheet : Sheet() {

    lateinit var func: FilterSheet.(view: ComposeView) -> Unit

    override fun onCreateLayoutView(): View {
        return ComposeView(requireContext()).apply {
            func(this)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                550.toPxInt
            )
        }
    }

    @OptIn(
        ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
        ExperimentalLayoutApi::class
    )
    @Composable
    fun SearchFilter(
        filters: Filters,
        onFilter: (List<String>?, String?, String?) -> Unit,
        genresState: Array<MutableState<Boolean>>?,
        yearsState: Array<MutableState<Boolean>>?,
        typesState: Array<MutableState<Boolean>>?
    ) {
        val titles = listOf(
            stringResource(id = R.string.genre),
            stringResource(id = R.string.year),
            stringResource(id = R.string.type)
        )
        val pagerState = rememberPagerState(0, 0.0f) {
            return@rememberPagerState titles.size
        }
        Scaffold {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(it)
                    .padding(horizontal = 16.dp)
                    .heightIn(max = 550.dp)
            ) {
                ScrollableTabRow(
                    modifier = Modifier
                        .weight(0.1f, false),
                    selectedTabIndex = pagerState.currentPage
                ) {
                    titles.forEachIndexed { index, title ->
                        Tab(text = {
                            Text(
                                title,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                lifecycleScope.launch {
                                    pagerState.scrollToPage(index)
                                }
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.8f, true)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .matchParentSize()
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier.verticalScroll(rememberScrollState(), true)
                        ) {
                            when (it) {
                                0 -> {
                                    filters.genres?.forEachIndexed { index, text ->
                                        FilterChip(
                                            selected = genresState?.getOrNull(index)?.value
                                                ?: false,
                                            onClick = {
                                                if (index < (genresState?.size ?: 0)) {
                                                    genresState?.getOrNull(index)?.value =
                                                        !(genresState?.getOrNull(index)?.value
                                                            ?: false)
                                                }
                                            },
                                            label = {
                                                Text(text = text)
                                            }
                                        )
                                    }
                                }

                                1 -> {
                                    filters.years?.forEachIndexed { index, text ->
                                        FilterChip(
                                            selected = yearsState?.getOrNull(index)?.value ?: false,
                                            onClick = {
                                                setOnlyOneItemChecked(yearsState, index)
                                            },
                                            label = {
                                                Text(text = text)
                                            })
                                    }
                                }

                                2 -> {
                                    filters.types?.forEachIndexed { index, text ->
                                        FilterChip(
                                            selected = typesState?.getOrNull(index)?.value ?: false,
                                            onClick = {
                                                setOnlyOneItemChecked(typesState, index)
                                            },
                                            label = {
                                                Text(text = text)
                                            })

                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .weight(0.1f, false)
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = {
                            val selectedGenres = filters.genres?.filterIndexed { index, _ ->
                                genresState?.getOrNull(index)?.value ?: false
                            }
                            val selectedYear = filters.years?.filterIndexed { index, _ ->
                                yearsState?.getOrNull(index)?.value ?: false
                            }?.firstOrNull()
                            val selectedType = filters.years?.filterIndexed { index, _ ->
                                typesState?.getOrNull(index)?.value ?: false
                            }?.firstOrNull()
                            onFilter(selectedGenres, selectedYear, selectedType)
                            dismiss()
                        }) {
                        Text(text = stringResource(id = R.string.apply))
                    }
                }
            }
        }
    }

    private fun setOnlyOneItemChecked(
        yearsState: Array<MutableState<Boolean>>?,
        index: Int
    ) {
        if (yearsState?.getOrNull(index)?.value == true) {
            yearsState.getOrNull(index)?.value = false
        } else {
            yearsState?.mapIndexed { stateIndex, _ ->
                yearsState.getOrNull(stateIndex)?.value =
                    (stateIndex == index)
            }
        }
    }

    fun show(ctx: Context, func: FilterSheet.(view: ComposeView) -> Unit): FilterSheet {
        this.windowContext = ctx
        this.func = func
        this.show()
        displayPositiveButton(false)
        displayNegativeButton(false)
        displayCloseButton()
        draggable(false)
        return this
    }

}