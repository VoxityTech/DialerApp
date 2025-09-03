package org.voxity.dialer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationItem(
    val id: Any,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String,
    val render: @Composable (Modifier) -> Unit = {}
)


object NavigationRegistry {
    private val _items = mutableListOf<NavigationItem>()
    val items: List<NavigationItem> get() = _items

    fun register(item: NavigationItem) {
        _items.add(item)
    }

    fun clear() {
        _items.clear()
    }
}



interface NavigationScreenRenderer {
    @Composable
    fun RenderScreen(
        screenId: Any,
        modifier: Modifier = Modifier
    )
}