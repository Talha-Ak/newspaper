package com.abdulkuddus.talha.newspaper.database

import androidx.room.Embedded
import androidx.room.Entity
import com.abdulkuddus.talha.newspaper.domain.News
import com.abdulkuddus.talha.newspaper.domain.Source
import java.util.*

/**
 * DatabaseEntities go in this file. These are responsible for persisting the necessary lottie_onboarding_setup into the database.
 * This allows Room to be modular, and can be re-architectured without dismantling other parts of the app.
 * Convert these to domain objects before using them in the UI.
 */

// Enum to limit category choices
enum class NewsCategories(val value: String){ PERSONAL("sources"), LOCAL("local"), SAVED("saved") }

// Enum to indicate saved status
enum class NewsSavedStatus { SAVED, NOT_SAVED, ERROR }

/**
 * [DatabaseNews] stores a single news article that can be saved into the database.
 */
@Entity(tableName = "news_articles_table", primaryKeys = ["url", "category"])
class DatabaseNews(
    val title: String,
    val description: String,
    val url: String,
    val imageUrl: String,
    @Embedded val source: DatabaseSource,
    val publishedAt: Date,
    val category: String
)

class DatabaseSource(
    val id: String,
    val name: String
)

fun List<DatabaseNews>.asDomainModel(): List<News> {
    return map {
        News(
            it.title, it.description, it.url, it.imageUrl, it.source.asDomainModel(), it.publishedAt, it.category
        )
    }
}

fun DatabaseSource.asDomainModel() = Source(this.id, this.name)