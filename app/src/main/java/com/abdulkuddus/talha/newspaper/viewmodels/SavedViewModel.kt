package com.abdulkuddus.talha.newspaper.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abdulkuddus.talha.newspaper.NewsRepository
import com.abdulkuddus.talha.newspaper.domain.News

/**
 * SavedViewModel is designed to store and manage UI-related lottie_onboarding_setup in a lifecycle conscious way. This
 * allows lottie_onboarding_setup to survive configuration changes such as screen rotations. In addition, background
 * work such as fetching network results can continue through configuration changes and deliver
 * results after the new Fragment or Activity is available.
 */
class SavedViewModel(repository: NewsRepository) : ViewModel() {

    val news = repository.getSavedNews()

    // Handle navigation to a specific article, use a backing property to expose an immutable version.
    private val _navigateToArticle = MutableLiveData<News>()
    val navigateToArticle: LiveData<News>
        get() = _navigateToArticle

    private val _navigateToSettings = MutableLiveData<Boolean>()
    val navigateToSettings: LiveData<Boolean>
        get() = _navigateToSettings

    /**
     * When the property is clicked, set the [_navigateToArticle] [MutableLiveData]
     * @param news The [News] that was clicked on.
     */
    fun displayNewsArticle(news: News) {
        _navigateToArticle.value = news
    }

    /**
     * After the navigation has taken place, make sure navigateToArticle is set to null
     */
    fun displayNewsArticleComplete() {
        _navigateToArticle.value = null
    }

    fun displaySettings() {
        _navigateToSettings.value = true
    }

    fun displaySettingsComplete() {
        _navigateToSettings.value = null
    }


    /**
     * Factory for constructing [SavedViewModel] with parameter
     */
    class Factory(val repository: NewsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SavedViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SavedViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}