package io.voxity.dialer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationItem(
    val id: Any,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
)

interface NavigationScreenRenderer {
    @Composable
    fun RenderScreen(
        screenId: Any,
        modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
    )
}