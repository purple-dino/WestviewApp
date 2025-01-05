package compose.wvhs.wvhsapp.Pages

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import compose.wvhs.wvhsapp.ViewModels.StudentSharedViewModel
import compose.wvhs.wvhsapp.ui.theme.AppTheme
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview


// Main app
@Composable
@Preview
fun App() {
    AppTheme {
    // Create initial variables
    val studentSharedViewModel: StudentSharedViewModel = viewModel { StudentSharedViewModel() }
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }
    val navController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(false) }
    // Create bottom navigation icons
    val items = listOf(
        BottomNavigationItem(
            title = "Schedule",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavigationItem(
            title = "Grades",
            selectedIcon = Icons.Filled.ThumbUp,
            unselectedIcon = Icons.Outlined.ThumbUp
        ),
        BottomNavigationItem(
            title = "Events",
            selectedIcon = Icons.Filled.DateRange,
            unselectedIcon = Icons.Outlined.DateRange
        ),
        BottomNavigationItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    // Create the ful page
    Scaffold(
        // Create bottom bar
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = navController.currentDestination?.route == item.title,
                            onClick = {
                                if (selectedItemIndex != index) {
                                    selectedItemIndex = index
                                    when (selectedItemIndex) {
                                        0 -> navController.navigate(ScheduleScreen)
                                        1 -> navController.navigate(GradesScreen)
                                        2 -> navController.navigate(EventsScreen)
                                        3 -> navController.navigate(SettingsScreen)
                                    }
                                }
                            },
                            label = { Text(
                                text = item.title
                            ) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedItemIndex == index) {
                                        item.selectedIcon
                                    } else {
                                        item.unselectedIcon
                                    },
                                    contentDescription = item.title
                                )
                            }, colors = NavigationBarItemDefaults.colors(unselectedIconColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                    }
                }
            }
        }) { paddingValues ->
            NavHost(navController = navController, startDestination = LoginScreen, modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())) {
                composable<LoginScreen> {
                    showBottomBar = false
                    if (selectedItemIndex != 0) {
                        selectedItemIndex = 0
                    }
                    LoginScreenFunc(navController, studentSharedViewModel)
                }
                composable<ScheduleScreen> {
                    if (selectedItemIndex != 0) {
                        selectedItemIndex = 0
                    }
                    showBottomBar = true
                    SchedulePageFunc(studentSharedViewModel, navController)
                }
                composable<GradesScreen>(
                    enterTransition = { fadeIn(animationSpec = tween(300)) },
                    exitTransition = { fadeOut(animationSpec = tween(150)) }
                ) {
                    if (selectedItemIndex != 1) {
                        selectedItemIndex = 1
                    }
                    showBottomBar = true
                    GradebookPage(navController, studentSharedViewModel)
                }
                composable<DetailedGradesScreen>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                            animationSpec = tween(300), towards = AnimatedContentTransitionScope.SlideDirection.Start
                        )
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                            animationSpec = tween(300), towards = AnimatedContentTransitionScope.SlideDirection.End
                        )
                    }
                ) {
                    if (selectedItemIndex != 1) {
                        selectedItemIndex = 1
                    }
                    val args = it.toRoute<DetailedGradesScreen>()
                    showBottomBar = true
                    DetailedGradesScreenFunc(studentSharedViewModel, args.activeClass, navController)
                }
                composable<SettingsScreen> {
                    if (selectedItemIndex != 3) {
                        selectedItemIndex = 3
                    }
                    showBottomBar = true
                    SettingsScreenFunc(studentSharedViewModel, navController)
                }
                composable<EventsScreen> {
                    if (selectedItemIndex != 2) {
                        selectedItemIndex = 2
                    }
                    showBottomBar = true
                    EventsPageFunc()
                }
            }
        }
    }
}

// Create the screen objects
@Serializable
object LoginScreen

@Serializable
object ScheduleScreen

@Serializable
object GradesScreen

@Serializable
object SettingsScreen

@Serializable
object EventsScreen

@Serializable
data class DetailedGradesScreen(
    var activeClass: String
)

// Create classes
data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)