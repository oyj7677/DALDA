package com.goody.dalda.ui.spirits_collection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SpiritsCollectionViewModel
@Inject
constructor() : ViewModel() {
    private val _text =
        MutableLiveData<String>().apply {
            value = "This is Spirits Collection Fragment"
        }
    val text: LiveData<String> = _text
}
