/*
 * Copyright  2021 Nazar Rusnak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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