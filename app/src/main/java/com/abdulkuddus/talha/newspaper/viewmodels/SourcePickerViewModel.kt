package com.abdulkuddus.talha.newspaper.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.abdulkuddus.talha.newspaper.NewsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * SourcePickerViewModel is designed to store and manage UI-related lottie_onboarding_setup in a lifecycle conscious way. This
 * allows lottie_onboarding_setup to survive configuration changes such as screen rotations. In addition, background
 * work such as fetching network results can continue through configuration changes and deliver
 * results after the new Fragment or Activity is available.
 *
 * @param application The application that this viewmodel is attached to, it's safe to hold a
 * reference to applications across rotation since Application is never recreated during activity
 * or fragment lifecycle events.
 */

class SourcePickerViewModel(val repository: NewsRepository, application: Application) : AndroidViewModel(application) {

    private val _sources = MutableLiveData<List<NewsRepository.SourceWithPref>>()
    val sources: LiveData<List<NewsRepository.SourceWithPref>>
        get() = _sources

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

    init {
        updateSourceList()
    }

    fun updateSourceChoice(source: NewsRepository.SourceWithPref) {
        repository.updateSourcePref(getApplication(), source)
    }

    fun updateSourceList(category: String? = null, language: String? = null) {
        _sources.value = null

        viewModelScope.launch {
            _sources.value = repository.getSources(getApplication(), category, language)
        }
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
            if (modelClass.isAssignableFrom(SourcePickerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SourcePickerViewModel(repository, app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}