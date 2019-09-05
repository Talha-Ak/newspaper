package com.abdulkuddus.talha.newspaper.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.abdulkuddus.talha.newspaper.databinding.NewsListItemBinding
import com.abdulkuddus.talha.newspaper.domain.News

/**
 * RecyclerView Adapter for setting up lottie_onboarding_setup binding on the items in the list.
 */
class NewsAdapter(val clickListener: NewsClick) : ListAdapter<News, NewsAdapter.ViewHolder>(DiffCallback) {

    /**
     * Called when RecyclerView needs a new [ViewHolder]  to represent an item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    /**
     * Called by RecyclerView to display the lottie_onboarding_setup at the specified position. This method should
     * update the contents of the [ViewHolder.binding] to reflect the item at the given
     * position.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    /**
     * ViewHolder for News items. Most of the work is done by lottie_onboarding_setup binding.
     */
    class ViewHolder private constructor(val binding: NewsListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Assign the binding object to the news item needed
         */
        fun bind(item: News, clickListener: NewsClick) {
            binding.apply {
                news = item
                this.clickListener = clickListener
                executePendingBindings()
            }
        }

        /**
         * Simple inflater that uses our lottie_onboarding_setup binding, so when the layout is inflated we have access to the binding object
         */
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = NewsListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    /**
     * Allows the [RecyclerView] to determine which items have changed when the list of [News]
     * has been updated.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<News>() {
        override fun areItemsTheSame(oldItem: News, newItem: News) = oldItem.url == newItem.url
        override fun areContentsTheSame(oldItem: News, newItem: News) = oldItem == newItem
    }
}

class NewsClick(val clickListener: (newsItem: News) -> Unit) {
    fun onClick(news: News) = clickListener(news)
}
