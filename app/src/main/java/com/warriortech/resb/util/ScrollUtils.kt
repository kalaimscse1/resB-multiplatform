package com.warriortech.resb.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Smoothly scrolls the list to the bottom.
 */
fun CoroutineScope.scrollToBottomSmooth(listState: LazyListState) {
    this.launch {
        val total = listState.layoutInfo.totalItemsCount
        if (total > 0) {
            listState.animateScrollToItem(total - 1)
        }
    }
}

/**
 * Keeps the last item visible above a BottomBar of given height.
 */
@Composable
fun LazyListState.ensureLastItemVisible(bottomBarHeight: Dp) {
    val scope = rememberCoroutineScope()
    val bottomBarPx = with(LocalDensity.current) { bottomBarHeight.toPx() }

    LaunchedEffect(this) {
        snapshotFlow { layoutInfo }
            .collect { layoutInfo ->
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()
                if (lastVisible != null) {
                    val viewportEnd = layoutInfo.viewportEndOffset
                    val itemEnd = lastVisible.offset + lastVisible.size

                    if (itemEnd > viewportEnd - bottomBarPx) {
                        scope.scrollToBottomSmooth(this@ensureLastItemVisible)
                    }
                }
            }
    }
}
