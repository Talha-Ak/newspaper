package com.abdulkuddus.talha.newspaper.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.abdulkuddus.talha.newspaper.utils.RoomConverters

/**
 * Room will implement this interface and create the DAO (Data Access Object) for us. This offers us
 * abstract access to our database.
 */
@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNews(vararg news: DatabaseNews)

    @Update
    fun updateNewsItem(vararg news: DatabaseNews)

    @Query("SELECT * from news_articles_table WHERE category = :category")
    fun getNewsFromCategory(category: String): LiveData<List<DatabaseNews>>

    @Query("SELECT * FROM news_articles_table WHERE url = :url")
    fun getNewsFromUrl(url: String): List<DatabaseNews>

    @Delete
    fun deleteNewsItem(vararg news: DatabaseNews)

    @Query("DELETE FROM news_articles_table WHERE category = :category")
    fun deleteNewsFromCategory(category: String)
}

/**
 * Small Database class that Room will implement
 */
@Database(entities = [DatabaseNews::class], version = 1, exportSchema = false)
@TypeConverters(RoomConverters::class)
abstract class NewsDatabase : RoomDatabase() {
    abstract val newsDao: NewsDao
}

/**
 * Singleton to ensure we only ever have a single instance of the database running
 */
private lateinit var INSTANCE: NewsDatabase

fun getDatabase(context: Context): NewsDatabase {
    synchronized(NewsDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                NewsDatabase::class.java,
                "news").build()
        }
    }
    return INSTANCE
}