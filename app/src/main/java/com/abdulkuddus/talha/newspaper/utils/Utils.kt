package com.abdulkuddus.talha.newspaper.utils

import android.content.Context
import android.net.ConnectivityManager
import android.text.format.DateUtils
import androidx.room.TypeConverter
import com.abdulkuddus.talha.newspaper.NewsRepository
import com.abdulkuddus.talha.newspaper.domain.Source
import java.util.*

/**
 * Conversion functions that convert to/from a Date/Long
 */
object RoomConverters {
    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: Date?): Long? = date?.time

}

/**
 * Extension function that formats dates to a relative time (E.g. "4 hours ago" or "Yesterday")
 */
fun Date.toFormattedString(): String =
    DateUtils.getRelativeTimeSpanString(this.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()

/**
 * Extension function that converts a comma separated string into a list.
 */
fun String.toMutableList(): MutableList<String> = this.split(",").map { it.trim() }.toMutableList()

/**
 * Extension function that converts a list into a comma separated string.
 */
fun MutableList<String>.toCommaString() = this.joinToString(",")


/**
 * Infix fun that converts a Source to a custom pair (with a mutable property)
 */
infix fun Source.withPref(isSaved: Boolean) = NewsRepository.SourceWithPref(this, isSaved)

/**
 * Queries the system to test for a network connection.
 */
fun networkIsAvailable(context: Context): Boolean {
    val conManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = conManager.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
}
