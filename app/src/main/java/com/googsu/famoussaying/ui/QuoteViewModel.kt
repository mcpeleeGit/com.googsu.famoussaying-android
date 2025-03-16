package com.googsu.famoussaying.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.googsu.famoussaying.api.QuoteApiClient
import com.googsu.famoussaying.data.Quote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuoteViewModel : ViewModel() {
    private val _currentQuote = MutableStateFlow<Quote?>(null)
    val currentQuote: StateFlow<Quote?> = _currentQuote.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _favoriteQuotes = MutableStateFlow<List<Quote>>(emptyList())
    val favoriteQuotes: StateFlow<List<Quote>> = _favoriteQuotes.asStateFlow()

    init {
        loadRandomQuote()
    }

    fun loadRandomQuote() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val response = QuoteApiClient.quoteApi.getRandomQuote()
                _currentQuote.value = Quote(
                    text = response.text ?: "명언이 없습니다.",
                    author = response.author ?: "작자 미상"
                )
            } catch (e: Exception) {
                _error.value = "명언을 불러오는데 실패했습니다."
                _currentQuote.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(quote: Quote) {
        val currentFavorites = _favoriteQuotes.value.toMutableList()
        if (currentFavorites.any { it.text == quote.text && it.author == quote.author }) {
            currentFavorites.removeAll { it.text == quote.text && it.author == quote.author }
        } else {
            currentFavorites.add(quote)
        }
        _favoriteQuotes.value = currentFavorites
    }

    fun setFavoriteQuotes(quotes: List<Quote>) {
        _favoriteQuotes.value = quotes
    }

    fun saveFavoriteQuotes(quotes: List<Quote>) {
        _favoriteQuotes.value = quotes
    }
} 