/*
 * Copyright 2019, The Android Open Source Project
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
 *
 */

package com.example.android.marsrealestate.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.marsrealestate.network.MarsApi
import com.example.android.marsrealestate.network.MarsApiFilter
import com.example.android.marsrealestate.network.MarsProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

enum class MarsApiStatus {LOADING,ERROR,DONE}
/**
 * The [ViewModel] that is attached to the [OverviewFragment].
 */
class OverviewViewModel : ViewModel() {

    // The internal MutableLiveData String that stores the most recent response
    private val _response = MutableLiveData<String>()

    // The external immutable LiveData for the response String
    val response: LiveData<String>
        get() = _response

    private val _properties = MutableLiveData<List<MarsProperty>>()
    val properties: LiveData<List<MarsProperty>>
    get() = _properties

    private val _apiStatus = MutableLiveData<MarsApiStatus>()
    val apiStatus : LiveData<MarsApiStatus>
    get() = _apiStatus

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    /**
     * Call getMarsRealEstateProperties() on init so we can display status immediately.
     */
    init {
        getMarsRealEstateProperties(MarsApiFilter.SHOW_ALL)
    }

    /**
     * Sets the value of the status LiveData to the Mars API status.
     */
    private fun getMarsRealEstateProperties(apiFilter: MarsApiFilter) {
        coroutineScope.launch {
            val getPropertiesDeferred = MarsApi.retrofitService.getProperties(apiFilter.value)
            try {
                _apiStatus.value = MarsApiStatus.LOADING
                val listResult = getPropertiesDeferred.await()
                //_response.value = "Success: ${listResult.size} properties retrieved"
                _apiStatus.value = MarsApiStatus.DONE
                _properties.value = listResult
            }
            catch (e: Exception) {
                //_response.value = "Failure: ${e.message}"
                _apiStatus.value = MarsApiStatus.ERROR
                //_properties.value = ArrayList()
            }
        }

        /*MarsApi.retrofitService.getProperties().enqueue(object : Callback<List<MarsProperty>> {
            override fun onFailure(call: Call<List<MarsProperty>>, t: Throwable) {
                _response.value = "Failure: " + t.message
            }

            override fun onResponse(call: Call<List<MarsProperty>>, response: Response<List<MarsProperty>>) {
                if(response.isSuccessful) {
                    if(response.body() != null) {
                        _response.value = "Success: ${response.body()?.size} properties retrieved"
                    }
                    else {
                        _response.value = "Empty response"
                    }
                }
                else {
                    _response.value = "Unsuccessful response"
                }
            }

        })*/
    }

    fun updateFilter(newFilter : MarsApiFilter) {
        getMarsRealEstateProperties(newFilter)
    }

    private val _navigateToDetail = MutableLiveData<MarsProperty?>()
    val navigateToDetail: LiveData<MarsProperty?>
        get() = _navigateToDetail

    fun onPropertyClicked(property: MarsProperty) {
        _navigateToDetail.value = property
    }

    fun doneNavigatingToDetail() {
        _navigateToDetail.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
