package com.example.pokedex.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.Database.PokedexCacheDatabase
import com.example.pokedex.data.model.DataPokemon
import com.example.pokedex.data.repository.PokemonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PokeDexViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PokedexCacheDatabase(application)
    private val repository = PokemonRepository()
    private var currentOffset = 0
    private var limit = 20
    var isLoading = false //có đang bận tải ko, tránh trường hợp user vuôt nhanh quá

    private var totalList = ArrayList<DataPokemon>()

    //du lieu pokemon
    private val _pokeList = MutableLiveData<List<DataPokemon>>()
    val pokeList: LiveData<List<DataPokemon>> = _pokeList


   fun fetchPokemonList() {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                // sang IO THREAD (Background) để gọi mạng & móc Database
                val newData = withContext(Dispatchers.IO) {
                    try {
                        // Có mạng
                        val pokemonList = repository.getPokemonInfo(limit, currentOffset)
                        if (pokemonList.isSuccessful && pokemonList.body() != null) {
                            val listDataRaw = pokemonList.body()!!.results
                            val listDataFormated = listDataRaw.map { item ->
                                async {
                                    val responseDetail = repository.getPokemonDetail(item.url)
                                    if (responseDetail.isSuccessful && responseDetail.body() != null) {
                                        val detail = responseDetail.body()!!
                                        DataPokemon(
                                            detail.id,
                                            detail.name.replaceFirstChar { it.uppercase() },
                                            detail.getSimpleTypes(),
                                            detail.getOfficialImageUrl()
                                        )
                                    } else null
                                }
                            }.awaitAll().filterNotNull()

                            // Lưu vào Database
                            if (listDataFormated.isNotEmpty()) {
                                db.insertCache(listDataFormated)
                            }

                            // Trả kết quả về cho hàm withContext
                            listDataFormated
                        } else {
                            emptyList<DataPokemon>()
                        }
                    } catch (e: Exception) {
                        // Mất mạng
                        Log.e(
                            "OFFLINE_MODE",
                            "Lỗi mạng: ${e.message}. Đang lấy data từ Database..."
                        )
                        // Trả kết quả từ Database về cho hàm withContext
                        db.getCache(limit, currentOffset)
                    }
                } // Kết thúc IO Thread

                // trở về MAIN THREAD, thoải mái cập nhật giao diện!
                if (newData.isNotEmpty()) {
                    totalList.addAll(newData)
                    _pokeList.value = ArrayList(totalList) // Bọc ArrayList để ListAdapter nhận diện
                    currentOffset += limit
                }
            } finally {
                isLoading = false
            }
        }
    }
}