package com.goody.dalda.ui.home

import com.goody.dalda.data.AlcoholData
import com.goody.dalda.data.local.BookmarkLocalDataSource

class FakeBookmarkLocalDataSourceImpl: BookmarkLocalDataSource {
    override fun insertAlcohol(alcoholData: AlcoholData) {
        TODO("Not yet implemented")
    }

    override fun deleteAlcohol(alcoholData: AlcoholData) {
        TODO("Not yet implemented")
    }

    override fun getBookmarkAlcoholList(): List<AlcoholData> {
        TODO("Not yet implemented")
    }

    override fun isBookMark(alcoholData: AlcoholData): Boolean {
        TODO("Not yet implemented")
    }
}