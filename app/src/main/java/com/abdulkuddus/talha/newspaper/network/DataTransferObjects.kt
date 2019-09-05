package com.abdulkuddus.talha.newspaper.network

import android.util.Log
import com.abdulkuddus.talha.newspaper.database.DatabaseNews
import com.abdulkuddus.talha.newspaper.database.DatabaseSource
import com.abdulkuddus.talha.newspaper.domain.Source
import com.squareup.moshi.JsonClass
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * DataTransferObjects go in this file. These are responsible for parsing responses from the server
 * or formatting objects to send to the server. Convert these to domain objects before
 * using them in the UI.
 */

enum class NewsFetchStatus { OK, NO_INTERNET, ERROR, HANDLED }

/**
 * [NetworkNewsContainer] holds a list of News articles.
 *
 * Used to parse first level of network result which looks like
 *
 * {
 *   "status": "ok",
 *   "totalResults": 10,
 *   "articles": []
 * }
 *
 */
@JsonClass(generateAdapter = true)
class NetworkNewsContainer(val status: String, val totalResults: Int, val articles: List<NetworkNews>)

/**
 * [NetworkSourceContainer] holds a list of News sources.
 */
@JsonClass(generateAdapter = true)
class NetworkSourceContainer(val status: String, val sources: List<NetworkSource>)

/**
 * [NetworkSource] represents the Source of the article
 */
@JsonClass(generateAdapter = true)
class NetworkSource(val id: String?, val name: String)

/**
 * [NetworkNews] represents a News article which can be read
 */
@JsonClass(generateAdapter = true)
class NetworkNews(
    val source: NetworkSource,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?,
    var category: String = "uncategorised"
)

/**
 * This extension function converts this Network News object to one that the database can use.
 */
fun NetworkNewsContainer.asDatabaseModel(category: String): Array<DatabaseNews> {
    val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    return articles.mapNotNull {
        try {
            DatabaseNews(
                it.title,
                it.description ?: "No description",
                it.url,
                it.urlToImage ?: "",
                it.source.asDatabaseModel(),
                formatter.parse(it.publishedAt),
                category
            )
        } catch (e: ParseException) {
            Log.e("NewsRepository from DTO", "Error parsing: $e")
            null
        }
    }
        .toTypedArray()
}

fun NetworkSource.asDatabaseModel() = DatabaseSource(this.id ?: "unknown", this.name)
fun List<NetworkSource>.asDomainModel() = this.map { Source(it.id ?: "unknown", it.name) }
