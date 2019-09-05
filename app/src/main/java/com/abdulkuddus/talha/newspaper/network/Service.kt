package com.abdulkuddus.talha.newspaper.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Since we only have one service, this can all go in one file.

const val API_KEY = "[REDACTED]"

/**
 * A retrofit service to fetch either user local news stories, or user selected new stories.
 */
interface NewsService {

    @GET("top-headlines")
    suspend fun getPersonalHeadlines(
        @Query("sources") source: String,
        @Query("pageSize") pageSize: Int,
        @Query("apiKey") apiKey: String): NetworkNewsContainer

    @GET("top-headlines")
    suspend fun getLocalHeadlines(
        @Query("country") country: String,
        @Query("pageSize") pageSize: Int,
        @Query("apiKey") apiKey: String): NetworkNewsContainer

    @GET("sources")
    suspend fun getSources(
        @Query("category") category: String? = null,
        @Query("language") language: String? = null,
        @Query("apiKey") apiKey: String): NetworkSourceContainer

}

/**
 * Main entry point for network access. To be called with 'Network.news.get_____()'
 */
object Network {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://newsapi.org/v2/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val news = retrofit.create(NewsService::class.java)
}