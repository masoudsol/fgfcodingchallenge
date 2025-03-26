package com.example.myapplicationfinal.Views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplicationfinal.ViewModels.RecommendationsViewModel
import com.google.accompanist.swiperefresh.*
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "list") {
        composable("list") { RecommendationListScreen(navController) }
        composable("detail/{title}/{description}") { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val description = backStackEntry.arguments?.getString("description") ?: ""
            DetailScreen(title, description)
        }
    }
}

@Composable
fun RecommendationListScreen(navController: NavHostController, viewModel: RecommendationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isRefreshing by remember { derivedStateOf { viewModel.isRefreshing } }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    SwipeRefresh(state = swipeRefreshState, onRefresh = { viewModel.fetchRecommendations() }) {
        LazyColumn(modifier = Modifier.padding(16.dp), state = listState) {
            items(viewModel.recommendations) { recommendation ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            navController.navigate("detail/${recommendation.title}/${recommendation.deck}")
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AsyncImage(
                            model = recommendation.promoImage.urls.`650`,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = recommendation.title, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = recommendation.deck,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            val isLiked = viewModel.likes[recommendation.title] ?: false
                            Button(onClick = { viewModel.toggleLike(recommendation.title) }) {
                                Text(if (isLiked) "Dislike" else "Like")
                            }
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= viewModel.recommendations.size - 3 && !viewModel.isLoadingMore) {
                    coroutineScope.launch { viewModel.loadMoreRecommendations() }
                }
            }
    }
}

@Composable
fun DetailScreen(title: String, description: String, viewModel: RecommendationsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var commentText by remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = description, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Comment Section
        Text(text = "Comments", style = MaterialTheme.typography.headlineMedium)
        LazyColumn(modifier = Modifier.height(150.dp)) {
            items(viewModel.comments[title] ?: emptyList()) { comment ->
                Text(text = comment, modifier = Modifier.padding(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            label = { Text("Add a comment") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (commentText.isNotBlank()) {
                viewModel.addComment(title, commentText)
                commentText = ""
            }
        }) {
            Text("Submit")
        }
    }
}