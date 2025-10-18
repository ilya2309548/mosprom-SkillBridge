package dev.mos.prom.utils.navigation

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MosPromBottomBar (
    navController : NavController,
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    BottomAppBar(
        containerColor = Color.White.copy(0.5f)
    ) {
        for (bar in Bar.items) {

            val isSelected = bar.route.toString() in currentRoute.toString()

            NavigationBarItem(
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(0.5f)
                ),
                onClick = {
                    navController.navigate(bar.route)
                },
                icon = {
                    Icon(
                        painter = painterResource(id = bar.icon),
                        contentDescription = bar.title,
                        tint = Color.Unspecified
                    )
                },
                label = {
                    if (isSelected) {
                        Text(
                            text = bar.title,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            fontWeight = FontWeight(weight = 600),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            )
        }
    }
}