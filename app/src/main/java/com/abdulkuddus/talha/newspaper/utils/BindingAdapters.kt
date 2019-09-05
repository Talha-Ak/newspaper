package com.abdulkuddus.talha.newspaper.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.abdulkuddus.talha.newspaper.domain.News
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

/**
 * These binding adapters allows custom binding logic to be implemented when the layout is used.
 * These adapters are used in the news_list_item.xml layout
 */

@BindingAdapter("newsTitle")
fun TextView.setNewsTitle(news: News?) {
    news?.let { text = news.title }
}

@BindingAdapter("newsPublisher")
fun TextView.setNewsPublisher(news: News?) {
    news?.let { text = news.source.name }
}

@BindingAdapter("newsDate")
fun TextView.setNewsDate(news: News?) {
    news?.let { text = news.date.toFormattedString() }
}

@BindingAdapter("newsImage")
fun ImageView.setNewsImage(news: News?) {
    news?.let { Glide.with(context)
        .load(news.imageUrl)
        .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(this) }
}