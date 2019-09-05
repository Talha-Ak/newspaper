package com.abdulkuddus.talha.newspaper.viewmodels

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abdulkuddus.talha.newspaper.NewsRepository
import com.abdulkuddus.talha.newspaper.database.NewsSavedStatus
import com.abdulkuddus.talha.newspaper.domain.News
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * DetailViewModel is designed to store and manage UI-related lottie_onboarding_setup in a lifecycle conscious way. This
 * allows lottie_onboarding_setup to survive configuration changes such as screen rotations. In addition, background
 * work such as fetching network results can continue through configuration changes and deliver
 * results after the new Fragment or Activity is available.
 */
class DetailViewModel(repository: NewsRepository, newsItem: News) : ViewModel() {

    /**
     * This is the job for all coroutines started by this ViewModel.
     *
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = Job()

    /**
     * This is the main scope for all coroutines launched by MainViewModel.
     *
     * Since we pass viewModelJob, All coroutines launched by uiScope can be cancelled by calling
     * viewModelJob.cancel()
     */
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val newsRepository = repository

    // Handle the specific article, use a backing property to expose an immutable version.
    private val _news = MutableLiveData<News>()
    val news: LiveData<News>
        get() = _news

    // Holds the save status of the article, also using a backing property for the same reason.
    private val _isNewsSaved = MutableLiveData<NewsSavedStatus>()
    val isNewsSaved: LiveData<NewsSavedStatus>
        get() = _isNewsSaved

    // Holds the url of article if user wants to view in browser.
    private val _viewInBrowser = MutableLiveData<Intent>()
    val viewInBrowser: LiveData<Intent>
        get() = _viewInBrowser

    private val _shareNews = MutableLiveData<Intent>()
    val shareNews: LiveData<Intent>
        get() = _shareNews

    /**
     * init{} is called immediately when this ViewModel is created.
     */
    init {
        _news.value = newsItem
    }

    /**
     * Called when the user requests to toggle save status of article. Executed in coroutine.
     */
    fun toggleSaveArticle() {
        viewModelScope.launch {
            _isNewsSaved.value = newsRepository.toggleSaveArticle(_news.value!!)
        }
    }

    /**
     * Reset the save status of article to prevent extra UI updates.
     */
    fun resetSavedStatus() {
        _isNewsSaved.value = null
    }

    fun showInBrowser(url: Uri? = null) {
        // Create intent with url
        _viewInBrowser.value = Intent(Intent.ACTION_VIEW, url ?: Uri.parse(_news.value?.url))
    }

    fun browserEventHandled() {
        _viewInBrowser.value = null
    }

    fun shareArticle() {
        _shareNews.value = Intent(Intent.ACTION_SEND).apply {
            putExtra(
                Intent.EXTRA_TEXT,
                "${news.value?.title} - ${news.value?.source?.name}: ${news.value?.url}"
            )
            type = "text/plain"
        }
    }

    fun shareEventHandled() {
        _shareNews.value = null
    }

    /**
     * Cancel all coroutines when the ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * Factory for constructing [DetailViewModel] with parameter
     */
    class Factory(val repository: NewsRepository, val newsItem: News) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DetailViewModel(repository, newsItem) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}