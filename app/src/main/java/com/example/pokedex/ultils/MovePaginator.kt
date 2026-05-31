package com.example.pokedex.ultils

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Scope

//Chu T co the la DataPokemonMove hay bottom sheet
class MovePaginator<T>(
    private val limit: Int = 20,
    private val fetchAction: suspend (List<String>) -> List<T>
) {
    private var currentRequestId = 0
    private var allUrls = listOf<String>()
    private var currentOffset = 0
    var isLoading = false
    var isLoadComplete = false

    private val totalList = ArrayList<T>()

    val liveData = MutableLiveData<List<T>>()

    fun initUrls(urls: List<String>, scope: CoroutineScope) {
        currentRequestId++
        val ticket = currentRequestId
        allUrls = urls
        currentOffset = 0
        totalList.clear()
        liveData.postValue(ArrayList())
        isLoadComplete = false

        scope.launch(Dispatchers.IO) {
            loadNextChunk(ticket)
        }
    }
    private suspend fun loadNextChunk(ticket: Int){
        if(currentOffset >= allUrls.size){
            isLoadComplete = true
            return
        }
        isLoading = true
        val nextUrls = allUrls.drop(currentOffset).take(limit)

        val newItems = fetchAction(nextUrls)

        if (ticket != currentRequestId) return
        if (newItems.isNotEmpty()){
            totalList.addAll(newItems)
            liveData.postValue(ArrayList(totalList))
            currentOffset += limit
        }
        isLoading = false
    }

    fun onScrollToBottom(scope: CoroutineScope){
        if(isLoading || isLoadComplete) return
        val ticket = currentRequestId
        scope.launch(Dispatchers.IO) {
            loadNextChunk(ticket)
        }
    }
}