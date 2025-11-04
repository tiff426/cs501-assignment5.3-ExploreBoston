package com.example.exploreboston

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.exploreboston.ui.theme.ExploreBostonTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MyViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExploreBostonTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
                MainView(viewModel)
            }
        }
    }
}

// where we keep our data

data class Location(val name : String, val moreInfo : String)
class MyViewModel : ViewModel() {
    val museums: SnapshotStateList<Location> = mutableStateListOf(Location("MFA", "465 Huntington Ave, Boston, MA 02115"), Location("MIT Museum", "Gambrill Center, 314 Main St, Cambridge, MA 02142"), Location("ISG", "25 Evans Way, Boston, MA 02115"))
    val parks: SnapshotStateList<Location> = mutableStateListOf(Location("Amory Park", "Freeman St &, Amory St, Brookline, MA 02446"), Location("Boston Public Garden", "4 Charles Street, Boston, MA 02116"), Location("Arnold Arboretum", "125 Arborway, Boston, MA 02130"))
    val restaurants: SnapshotStateList<Location> = mutableStateListOf(Location("Nud Pob", "738 Commonwealth Ave, Boston, MA 02215"))

    fun getList(category : String) : SnapshotStateList<Location> {
        when (category) {
            "museums" -> {
                return museums
            }

            "parks" -> {
                return parks
            }

            else -> {
                return restaurants
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ExploreBostonTheme {
        Greeting("Android")
    }
}

//Build an app with 4+ destinations that mimic a 'tour' of a city.
// Users should be able to navigate deeper into screens and
// back out while maintaining correct stack behavior.
//
//Requirements:
//• DONE Start with Home screen (Intro)
//• DONE Categories screen (e.g., Museums, Parks, Restaurants)
//• DONE List screen (e.g., “All Museums”)
//• DONE Detail screen (e.g., “MIT Museum”)
//• DONE Use arguments to pass the selected category and location ID down the navigation path
//• Implement stack management such that going 'Home' clears the stack (popUpTo with inclusive = true)
//• Use both String and Int arguments in route declarations (NavType.IntType)
//• Clearly demonstrate use of NavController.navigate() with structured route strings
//• Use rememberNavController() properly and keep navigation logic clean in a separate NavGraph file
//• Add a reusable TopAppBar or BottomBar across all screens; Implement logic that disables back button after reaching Home via a full navigation cycle

// making screens first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(navController: NavController, title: String) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val canGoBack = currentRoute != Routes.Home.route

    TopAppBar(
        title = { Text("Explore Boston") },
        navigationIcon = if (canGoBack) {
            {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        } else {
            {} // empty composable instead of null
        }
    )
}

@Composable
fun MyBottomBar(navController: NavController) {
    val bottomNavScreens = listOf(Routes.Home, Routes.Categories)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar {
        bottomNavScreens.forEach { screen ->
            NavigationBarItem(
                label = { Text(screen.title) }, // The text label for the item.
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = screen.title
                    )
                }, // The icon for the item.

                // 5. Determine if this item is currently selected.
                // We check if the current route is part of the destination's hierarchy.
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,

                // 6. Define the click action for the item.
                onClick = {
                    if (screen.route == Routes.Home.route) {
                        // Go to Home and clear entire stack
                        navController.navigate(screen.route) {
                            popUpTo(0) { inclusive = true } // clear everything
                            launchSingleTop = true
                        }
                    } else {
                        // Navigate to other screens normally
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "this is home base")
        Text(text = "use the nav bar to get around!")
    }
}

@Composable
fun CategoriesScreen(viewModel: MyViewModel, navController: NavController) {
    Column() {
        Text(text = "all categories")
        Card(
            modifier = Modifier.padding(30.dp)
            .size(width = 300.dp, height = 200.dp),
            onClick = {
                navController.navigate(
                    Routes.Museums.route
                )
            } // navigate to museums page
            ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "museums")
            }
        }
        Card(
            modifier = Modifier.padding(30.dp)
                .size(width = 300.dp, height = 200.dp),
            onClick = {
                navController.navigate(Routes.Parks.route)
            } // navigate to museums page
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "parks")
            }
        }
        Card(
            modifier = Modifier.padding(30.dp)
                .size(width = 300.dp, height = 200.dp),
            onClick = {
                navController.navigate(Routes.Restaurants.route)
            } // navigate to museums page
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "restaurants")
            }
        }
    }
}

@Composable
fun ListScreen(navController : NavController, viewModel : MyViewModel, category : String) {
    val list = viewModel.getList(category)

    Column() {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(list) { index, location ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.LightGray
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = location.name) // show the note
                    Button(onClick = {
                        navController.navigate("${Routes.Details.route}/$category/$index")
                    }) {
                        Text("see more info")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsScreen(navController: NavController, category : String, index : Int, viewModel: MyViewModel) {
    val list = viewModel.getList(category)
    val location = list.getOrNull(index)

    Column(modifier = Modifier.padding(16.dp)) {
//        Button(onClick = { navController.popBackStack() }) {
//            Text("Back")
//        }

//        Spacer(modifier = Modifier.size(8.dp))

        if (location != null) {
            Text(text = location.name)
//            Spacer(modifier = Modifier.size(4.dp))
            Text(text = "Address: ${location.moreInfo}")
        } else {
            Text("Location not found")
        }
    }
}

sealed class Routes(val route : String, val title : String, val icon : ImageVector) {
    object Home : Routes("home", "Home", Icons.Default.Home)
    object Categories : Routes("categories", "Categories", Icons.Default.Menu)
    object Museums : Routes("museums", "List", Icons.Default.Favorite)
    object Parks : Routes("parks", "List", Icons.Default.Favorite)
    object Restaurants : Routes("restaurants", "List", Icons.Default.Favorite)
    object Details : Routes("details", "Details", Icons.Default.Info)
}

val bottomNavScreens = listOf(
    Routes.Home,
    Routes.Categories
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(viewModel : MyViewModel) {
    val navController = rememberNavController()
    Scaffold(
        topBar = { MyTopAppBar(navController, "Explore Boston") },
        bottomBar = { MyBottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home.route, // The first screen to show.
            modifier = Modifier.padding(innerPadding) // Apply padding from the Scaffold.
        ) {
            composable(Routes.Home.route) {
                HomeScreen()
            }

            composable(Routes.Categories.route) {
                CategoriesScreen(viewModel, navController)
            }

            composable(Routes.Museums.route) {
                ListScreen(navController, viewModel, "museums")
            }

            composable(Routes.Parks.route) {
                ListScreen(navController, viewModel, "parks")
            }

            composable(Routes.Restaurants.route) {
                ListScreen(navController, viewModel, "restaurants")
            }

            composable(
                route = Routes.Details.route + "/{category}/{index}",
                arguments = listOf(
                    navArgument("category") { type = NavType.StringType },
                    navArgument("index") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                val index = backStackEntry.arguments?.getInt("index") ?: 0

                DetailsScreen(
                    navController = navController,
                    category = category,
                    index = index,
                    viewModel = viewModel
                )
            }

        }

    }
}
