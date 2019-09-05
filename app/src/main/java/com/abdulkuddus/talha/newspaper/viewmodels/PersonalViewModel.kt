package com.abdulkuddus.talha.newspaper.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.abdulkuddus.talha.newspaper.NewsRepository
import com.abdulkuddus.talha.newspaper.database.NewsCategories
import com.abdulkuddus.talha.newspaper.domain.News
import com.abdulkuddus.talha.newspaper.domain.NewsWithStatus
import com.abdulkuddus.talha.newspaper.network.NewsFetchStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * PersonalViewModel is designed to store and manage UI-related lottie_onboarding_setup in a lifecycle conscious way. This
 * allows lottie_onboarding_setup to survive configuration changes such as screen rotations. In addition, background
 * work such as fetching network results can continue through configuration changes and deliver
 * results after the new Fragment or Activity is available.
 *
 * @param app The application that this viewmodel is attached to, it's safe to hold a
 * reference to applications across rotation since Application is never recreated during activity
 * or fragment lifecycle events.
 */
class PersonalViewModel(private val repository: NewsRepository, app: Application) : AndroidViewModel(app) {

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

    val news: LiveData<NewsWithStatus> = repository.getLiveNews(NewsCategories.PERSONAL)

    // Handle navigation to a specific article, use a backing property to expose an immutable version.
    private val _navigateToArticle = MutableLiveData<News>()
    val navigateToArticle: LiveData<News>
        get() = _navigateToArticle

    private val _navigateToSettings = MutableLiveData<Boolean>()
    val navigateToSettings: LiveData<Boolean>
        get() = _navigateToSettings


    fun refreshNews() {
        setListAsHandled()
        viewModelScope.launch {
            repository.refreshNews(getApplication(), NewsCategories.PERSONAL)
        }
    }

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

    fun setListAsHandled() {
        repository.setListStatus(NewsFetchStatus.HANDLED)
    }

    /**
     * Cancel all coroutines when the ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * Factory for constructing [PersonalViewModel] with parameter
     */
    class Factory(val repository: NewsRepository, val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PersonalViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PersonalViewModel(repository, app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}