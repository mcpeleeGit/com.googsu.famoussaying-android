package com.googsu.famoussaying

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.googsu.famoussaying.data.Quote
import com.googsu.famoussaying.ui.AuthorScreen
import com.googsu.famoussaying.ui.QuoteViewModel
import com.googsu.famoussaying.ui.theme.FamousSayingTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel: QuoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSharedPreferences()
        loadFavoriteQuotes()
        viewModel.loadRandomQuote()

        setContent {
            FamousSayingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }

    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("FavoriteQuotes", MODE_PRIVATE)
    }

    private fun loadFavoriteQuotes() {
        val gson = Gson()
        val json = sharedPreferences.getString("favorite_quotes", null)
        if (json != null) {
            val type = object : TypeToken<List<Quote>>() {}.type
            val quotes = gson.fromJson<List<Quote>>(json, type)
            viewModel.setFavoriteQuotes(quotes)
        }
    }

    fun saveFavoriteQuotes(quotes: List<Quote>) {
        val gson = Gson()
        val json = gson.toJson(quotes)
        sharedPreferences.edit().putString("favorite_quotes", json).apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: QuoteViewModel) {
    val context = LocalContext.current
    var showFavorites by remember { mutableStateOf(false) }
    var showAuthorInfo by remember { mutableStateOf<String?>(null) }
    
    val currentQuote by viewModel.currentQuote.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val favoriteQuotes by viewModel.favoriteQuotes.collectAsState()

    // 즐겨찾기 변경 시 저장
    LaunchedEffect(favoriteQuotes) {
        (context as MainActivity).saveFavoriteQuotes(favoriteQuotes)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (showFavorites) "즐겨찾기 목록" else "명언") },
                navigationIcon = {
                    IconButton(onClick = { showFavorites = !showFavorites }) {
                        Icon(
                            imageVector = if (showFavorites) Icons.Default.Refresh else Icons.Default.List,
                            contentDescription = if (showFavorites) "새로고침" else "즐겨찾기 목록"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            showAuthorInfo != null -> {
                AuthorScreen(
                    author = showAuthorInfo!!,
                    onNavigateBack = { showAuthorInfo = null }
                )
            }
            showFavorites -> {
                FavoriteQuotesScreen(
                    favoriteQuotes = favoriteQuotes,
                    onRemoveFavorite = { quote ->
                        viewModel.toggleFavorite(quote)
                    },
                    onAuthorClick = { author ->
                        showAuthorInfo = author
                    }
                )
            }
            else -> {
                QuoteScreen(
                    currentQuote = currentQuote,
                    isLoading = isLoading,
                    error = error,
                    onRefresh = { viewModel.loadRandomQuote() },
                    onToggleFavorite = { quote ->
                        viewModel.toggleFavorite(quote)
                    },
                    onAuthorClick = { author ->
                        showAuthorInfo = author
                    }
                )
            }
        }
    }
}

@Composable
fun QuoteScreen(
    currentQuote: Quote?,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onToggleFavorite: (Quote) -> Unit,
    onAuthorClick: (String) -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        } else if (currentQuote != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("명언", currentQuote.text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "명언이 복사되었습니다", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentQuote.text ?: "명언이 없습니다.",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = currentQuote.author ?: "작자 미상",
                                fontSize = 16.sp,
                                textAlign = TextAlign.End
                            )
                            IconButton(
                                onClick = { onAuthorClick(currentQuote.author ?: "작자 미상") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "작가 정보",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "${currentQuote.text}\n- ${currentQuote.author}")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "명언 공유하기"))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "공유하기",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            IconButton(
                                onClick = { onToggleFavorite(currentQuote) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "즐겨찾기",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                text = "명언을 불러오는 중...",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onRefresh,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("다른 명언 보기")
        }
    }
}

@Composable
fun FavoriteQuotesScreen(
    favoriteQuotes: List<Quote>,
    onRemoveFavorite: (Quote) -> Unit,
    onAuthorClick: (String) -> Unit
) {
    val context = LocalContext.current
    
    if (favoriteQuotes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "저장된 명언이 없습니다.",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 48.dp)
        ) {
            items(favoriteQuotes) { quote ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("명언", quote.text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "명언이 복사되었습니다", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = quote.text ?: "명언이 없습니다.",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = quote.author ?: "작자 미상",
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.End
                                )
                                IconButton(
                                    onClick = { onAuthorClick(quote.author ?: "작자 미상") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "작가 정보",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "${quote.text}\n- ${quote.author}")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "명언 공유하기"))
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "공유하기",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                IconButton(
                                    onClick = { onRemoveFavorite(quote) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "즐겨찾기에서 제거",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}