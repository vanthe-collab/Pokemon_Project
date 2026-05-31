package com.example.pokedex.data.repository

import android.telecom.Call
import com.example.pokedex.data.APIService.RetrofitClient

class PokemonRepository {
    val apiService = RetrofitClient.apiService
    val apiItemService = RetrofitClient.apiItemService
    suspend fun getPokemonInfo(limit: Int, offset: Int) = apiService.getPokemonList(limit, offset)
    suspend fun getPokemonStats(id: Int) = apiService.getPokemonStats(id)
    suspend fun getPokemonSpeciesUrl(url: String) = apiService.getPokemonSpeciesByUrl(url)
    suspend fun getPokemonDetail(url: String) = apiService.getPokemonDetail(url)
    suspend fun getPokemonEvolution(url: String) = apiService.getEvolutionChain(url)
    suspend fun getItemList(limit: Int, offset: Int) = apiItemService.getItemList(limit, offset)
    suspend fun getItemListDetail(url: String) = apiItemService.getItemDetail(url)
    suspend fun getItemsByCategory(category: String) = apiItemService.getItemsByCategory(category)
    suspend fun getAllBerries() = apiItemService.getAllBerries()
    suspend fun getMoveDetail(url: String) = apiService.getMoveDetail(url)

}