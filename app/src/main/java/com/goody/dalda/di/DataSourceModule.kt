package com.goody.dalda.di

import com.goody.dalda.data.local.BookmarkLocalDataSource
import com.goody.dalda.data.local.BookmarkLocalDataSourceImpl
import com.goody.dalda.data.local.PreferenceLocalDataSource
import com.goody.dalda.data.local.PreferenceLocalDataSourceImpl
import com.goody.dalda.data.local.SearchLocalDataSource
import com.goody.dalda.data.local.SearchLocalDataSourceImpl
import com.goody.dalda.data.remote.NoticeRemoteDataSource
import com.goody.dalda.data.remote.NoticeRemoteDataSourceImpl
import com.goody.dalda.data.remote.UserRemoteDataSource
import com.goody.dalda.data.remote.UserRemoteDataSourceImpl
import com.goody.dalda.data.remote.blog.NaverBlogDataSource
import com.goody.dalda.data.remote.blog.NaverBlogDataSourceImpl
import com.goody.dalda.data.remote.home.AlcoholDataRemoteDataSource
import com.goody.dalda.data.remote.home.AlcoholDataRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    abstract fun bindUserRemoteDataSource(dataSource: UserRemoteDataSourceImpl): UserRemoteDataSource

    @Binds
    abstract fun bindAlcoholDataRemoteDataSource(repository: AlcoholDataRemoteDataSourceImpl): AlcoholDataRemoteDataSource

    @Binds
    abstract fun bindPreferenceLocalDataSource(dataSource: PreferenceLocalDataSourceImpl): PreferenceLocalDataSource

    @Binds
    abstract fun bindBookmarkDatabase(bookmarkLocalDataSource: BookmarkLocalDataSourceImpl): BookmarkLocalDataSource

    @Binds
    abstract fun bindNoticeRemoteDataSource(dataSource: NoticeRemoteDataSourceImpl): NoticeRemoteDataSource

    @Binds
    abstract fun bindNaverBlogDataSource(dataSource: NaverBlogDataSourceImpl): NaverBlogDataSource

    @Binds
    abstract fun bindSearchDataSource(dataSource: SearchLocalDataSourceImpl): SearchLocalDataSource
}
