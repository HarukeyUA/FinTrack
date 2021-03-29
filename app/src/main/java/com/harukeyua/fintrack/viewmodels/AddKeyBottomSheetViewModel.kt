package com.harukeyua.fintrack.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.harukeyua.fintrack.repos.MonobankApiRepo
import com.harukeyua.fintrack.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddKeyBottomSheetViewModel @Inject constructor(private val repo: MonobankApiRepo) :
    ViewModel() {

    private val _incorrectKeySizeError = MutableLiveData<Event<Int>>()
    val incorrectKeySizeError: LiveData<Event<Int>>
        get() = _incorrectKeySizeError

    var apiKey: String = ""
        private set

    private val _apiKey = MutableLiveData<String>()

    val clientInfoStatus = Transformations.switchMap(_apiKey) { key ->
        repo.initialMonoClientInfoSync(key)
    }

    fun verifyKey(key: String) {
        if (key.length != 44) {
            _incorrectKeySizeError.value = Event(key.length)
        } else {
            _apiKey.value = key
            apiKey = key
        }
    }
}