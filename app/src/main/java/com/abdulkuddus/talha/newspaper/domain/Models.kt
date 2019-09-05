package com.abdulkuddus.talha.newspaper.domain

import android.os.Parcelable
import com.abdulkuddus.talha.newspaper.database.DatabaseNews
import com.abdulkuddus.talha.newspaper.database.DatabaseSource
import com.abdulkuddus.talha.newspaper.network.NewsFetchStatus
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Domain objects are plain Kotlin lottie_onboarding_setup classes that represent the things in our app. These are the
 * objects that should be displayed on screen, or manipulated by the app.
 *
 * See database package for objects that are mapped to the database
 * See network package for objects that parse or prepare network calls
 */

/**
 * News represents an article that can be read
 */
@Parcelize
data class News(
    val title: String,
    val description: String,
    val url: String,
    val imageUrl: String,
    val source: Source,
    val date: Date,
    var category: String) : Parcelable

@Parcelize
data class Source(val id: String, val name: String) : Parcelable

/**
 * Used in conjunction with repository to notify UI of connection issues / errors
 */
class NewsWithStatus(val status: NewsFetchStatus, val articles: List<News>?)

fun News.asDatabaseModel(): DatabaseNews {
    return DatabaseNews(this.title, this.description, this.url,
        this.imageUrl, this.source.asDatabaseModel(), this.date, this.category)
}

fun Source.asDatabaseModel(): DatabaseSource = DatabaseSource(this.id, this.name)