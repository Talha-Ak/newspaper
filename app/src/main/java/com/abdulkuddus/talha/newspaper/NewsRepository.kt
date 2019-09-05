package com.abdulkuddus.talha.newspaper

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
import com.abdulkuddus.talha.newspaper.database.*
import com.abdulkuddus.talha.newspaper.domain.News
import com.abdulkuddus.talha.newspaper.domain.NewsWithStatus
import com.abdulkuddus.talha.newspaper.domain.Source
import com.abdulkuddus.talha.newspaper.domain.asDatabaseModel
import com.abdulkuddus.talha.newspaper.network.*
import com.abdulkuddus.talha.newspaper.utils.networkIsAvailable
import com.abdulkuddus.talha.newspaper.utils.toCommaString
import com.abdulkuddus.talha.newspaper.utils.toMutableList
import com.abdulkuddus.talha.newspaper.utils.withPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class NewsRepository private constructor(private val newsDao: NewsDao) {

    /**
     * LiveData holding the status of the news articles
     */
    private val newsStatus = MutableLiveData<NewsFetchStatus>()

    /**
     * Function that gets news backed by network + database. It creates a [MediatorLiveData] and
     * combines the news list from database and event status.
     */
    fun getLiveNews(category: NewsCategories): LiveData<NewsWithStatus> {
        val news = MediatorLiveData<NewsWithStatus>()
        val newsList = newsDao.getNewsFromCategory(category.value)
        // Set to null to avoid status from another ViewModel leaking into this one.
        newsStatus.value = null
        news.addSource(newsList) {
            Log.i("NewsRepository", "db LiveData updated, has observers: ${news.hasActiveObservers()}")
            news.value = combineLiveData(newsStatus, newsList, true)
        }
        news.addSource(newsStatus) {
            Log.i("NewsRepository", "status LiveData updated, has observers: ${news.hasActiveObservers()}")
            news.value = combineLiveData(newsStatus, newsList, false)
        }
        return news
    }

    /**
     * Simple logic that determines the result of output LiveData based on 2 input LiveData
     */
    private fun combineLiveData(
        status: LiveData<NewsFetchStatus>, list: LiveData<List<DatabaseNews>>, fromDb: Boolean
    ): NewsWithStatus {

        return if (fromDb && (!list.value.isNullOrEmpty() || status.value == null)) {
            NewsWithStatus(NewsFetchStatus.OK, list.value?.asDomainModel())
        } else {
            NewsWithStatus(status.value ?: NewsFetchStatus.HANDLED, list.value?.asDomainModel())
        }
    }

    fun setListStatus(status: NewsFetchStatus) {
        newsStatus.value = status
    }

    fun getSavedNews(): LiveData<List<News>> =
        Transformations.map(newsDao.getNewsFromCategory(NewsCategories.SAVED.value)) {
            it.asDomainModel()
        }

    /**
     * Refresh the articles stored in the offline cache.
     *
     * This function uses the IO dispatcher to ensure the database insert database operation
     * happens on the IO dispatcher. By switching to the IO dispatcher using `withContext` this
     * function is now safe to call from any thread including the Main thread.
     */
    suspend fun refreshNews(context: Context, category: NewsCategories) {
        Log.i("NewsRepository", "Fetching News...")
        return withContext(Dispatchers.IO) {

            // Jump out of coroutine if no internet
            if (!networkIsAvailable(context)) {
                withContext(Dispatchers.Main) { newsStatus.value = NewsFetchStatus.NO_INTERNET }
                return@withContext
            }

            // Get User prefs before querying
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val country = sharedPref.getString(context.resources.getString(R.string.pref_key_country), "gb")!!
            val source = sharedPref.getString(context.resources.getString(R.string.pref_key_sources), "bbc-news")!!

            // Query from the appropriate API endpoint
            try {
                val newsArticles = when (category) {
                    NewsCategories.PERSONAL -> Network.news.getPersonalHeadlines(source, 40, API_KEY)
                    NewsCategories.LOCAL -> Network.news.getLocalHeadlines(country, 40, API_KEY)
                    else -> {
                        withContext(Dispatchers.Main) { newsStatus.value = NewsFetchStatus.ERROR }
                        return@withContext
                    }
                }
                Log.i("NewsRepository", "News fetched")
                // If empty, no point deleting existing news. Notify error and return.
                if (newsArticles.articles.isEmpty()) {
                    newsStatus.value = NewsFetchStatus.ERROR
                    return@withContext
                }

                // Remove all existing News Items in the category and replace them
                newsDao.deleteNewsFromCategory(category.value)
                Log.i("NewsRepository", "News deleted")
                newsDao.insertNews(*newsArticles.asDatabaseModel(category.value))
                Log.i("NewsRepository", "News inserted")
            } catch (e: Exception) {
                Log.e("NewsRepository", "Error in fetching data: $e")
                withContext(Dispatchers.Main) { newsStatus.value = NewsFetchStatus.ERROR }
                throw e
            }
        }
    }

    /**
     * Change an article's save status.
     *
     * This function searches for all matching articles with the same url, checks to see if any
     * of them are saved, and inverts their status.
     */
    suspend fun toggleSaveArticle(newsItem: News): NewsSavedStatus {
        return withContext(Dispatchers.IO) {
            try {
                val matchingArticles = newsDao.getNewsFromUrl(newsItem.url)
                val savedArticle = matchingArticles.find { it.category == NewsCategories.SAVED.value }
                if (savedArticle == null) {
                    newsItem.category = NewsCategories.SAVED.value
                    newsDao.insertNews(newsItem.asDatabaseModel())
                    NewsSavedStatus.SAVED
                } else {
                    newsDao.deleteNewsItem(savedArticle)
                    NewsSavedStatus.NOT_SAVED
                }
            } catch (e: Exception) {
                Log.e("NewsRepository", "Error saving article: $e")
                NewsSavedStatus.ERROR
            }
        }
    }

    suspend fun updateAllInBackground(context: Context): Boolean {
        for (category in listOf(NewsCategories.PERSONAL, NewsCategories.LOCAL)) {
            refreshNews(context, category)
            if (newsStatus.value == NewsFetchStatus.ERROR) return false
        }
        return true
    }

    data class SourceWithPref(val source: Source, var isSaved: Boolean)

    suspend fun getSources(
        context: Context, category: String? = null, language: String? = null): List<SourceWithPref>? {

        return withContext(Dispatchers.IO) {

            if (!networkIsAvailable(context)) {
                TODO("return with NO INTERNET")
            }

            try {

                val networkSources =
                    Network.news.getSources(category, language, API_KEY).sources.asDomainModel()

                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                val savedSourceIds: List<String> =
                    sharedPref.getString(context.getString(R.string.pref_key_sources), "")
                        .toMutableList()
                networkSources.map { it withPref (it.id in (savedSourceIds)) }
            } catch (e: HttpException) {
                Log.e("NewsRepository", "Error fetching sources: $e")
                null
            }
        }
    }

    fun updateSourcePref(context: Context, sourceWithPref: SourceWithPref) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val savedSourceIds = sharedPref.getString(context.getString(R.string.pref_key_sources), "").toMutableList()
        if (sourceWithPref.isSaved) {
            if (sourceWithPref.source.id !in savedSourceIds) savedSourceIds.add(sourceWithPref.source.id)
        } else {
            if (sourceWithPref.source.id in savedSourceIds) savedSourceIds.remove(sourceWithPref.source.id)
        }
        sharedPref.edit { putString(context.getString(R.string.pref_key_sources), savedSourceIds.toCommaString()) }
    }

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: NewsRepository? = null

        fun getInstance(newsDao: NewsDao) =
            instance ?: synchronized(this) {
                instance ?: NewsRepository(newsDao).also { instance = it }
            }
    }

}