package com.abdulkuddus.talha.newspaper.utils

import android.app.Application
import android.content.Context
import com.abdulkuddus.talha.newspaper.NewsRepository
import com.abdulkuddus.talha.newspaper.database.getDatabase
import com.abdulkuddus.talha.newspaper.domain.News
import com.abdulkuddus.talha.newspaper.viewmodels.*

object Injector {

    fun getNewsRepository(context: Context): NewsRepository =
        NewsRepository.getInstance(getDatabase(context.applicationContext).newsDao)

    fun providePersonalViewModelFactory(application: Application): PersonalViewModel.Factory =
        PersonalViewModel.Factory(getNewsRepository(application), application)

    fun provideLocalViewModelFactory(application: Application): LocalViewModel.Factory =
        LocalViewModel.Factory(getNewsRepository(application), application)

    fun provideSavedViewModelFactory(context: Context): SavedViewModel.Factory =
        SavedViewModel.Factory(getNewsRepository(context))

    fun provideDetailViewModelFactory(context: Context, newsItem: News): DetailViewModel.Factory =
        DetailViewModel.Factory(getNewsRepository(context), newsItem)

    fun provideSourcePickerViewModelFactory(application: Application): SourcePickerViewModel.Factory =
        SourcePickerViewModel.Factory(getNewsRepository(application), application)

}