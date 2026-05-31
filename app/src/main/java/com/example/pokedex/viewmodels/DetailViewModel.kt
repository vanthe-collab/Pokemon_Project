package com.example.pokedex.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.Database.PokedexCacheDatabase
import com.example.pokedex.data.repository.EvolutionMapper
import com.example.pokedex.data.model.DataPokemon
import com.example.pokedex.data.model.DataPokemonMove
import com.example.pokedex.data.model.DataPokemonMoveForSelection // Nhớ import cái này
import com.example.pokedex.data.model.EvolutionItem
import com.example.pokedex.data.model.PokemonResponseStats
import com.example.pokedex.data.model.PokemonSpeciesResponse
import com.example.pokedex.data.repository.PokemonRepository
import com.example.pokedex.ultils.MovePaginator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    //Khoi tao database
    private val db = PokedexCacheDatabase(application)
    private val repository = PokemonRepository()
    private val evolutionMap = EvolutionMapper()
    private val idHistoryStack = mutableListOf<Int>()
    private var currentPokemonId = -1

    private val _pokeEvolutionItem = MutableLiveData<List<EvolutionItem>>()
    val pokeEvolutionItem: LiveData<List<EvolutionItem>> = _pokeEvolutionItem

    private val _fragmentStats = MutableLiveData<PokemonResponseStats>()
    val fragmentStats: LiveData<PokemonResponseStats> = _fragmentStats

    private val _pokemonSpeciesResponse = MutableLiveData<PokemonSpeciesResponse>()
    val pokemonSpeciesResponse: LiveData<PokemonSpeciesResponse> = _pokemonSpeciesResponse

    private val _bgColorId = MutableLiveData<Int>()
    val bgColorId: LiveData<Int> = _bgColorId

    private val _detailPokemon = MutableLiveData<DataPokemon>()
    val detailPokemon: LiveData<DataPokemon> = _detailPokemon

    // 1.Tab Moves (Màn hình chi tiết)
    val moveTabPaginator = MovePaginator<DataPokemonMove> { urls ->
        getMoveDetails(urls)
    }

    //Kiểm tra xem có mạng khi vào detail ko
    private val _isOfflineMode = MutableLiveData<Boolean>()
    val isOfflineMode: LiveData<Boolean> = _isOfflineMode


    // 2.BottomSheet (Màn hình chọn chiêu)
    val selectionPaginator = MovePaginator<DataPokemonMoveForSelection> { urls ->
        val rawMoves = getMoveDetails(urls)
        rawMoves.map { raw ->
            DataPokemonMoveForSelection(
                name = raw.name, type = raw.type, damageClass = raw.damageClass,
                power = raw.power, accuracy = raw.accuracy, pp = raw.pp,
                description = raw.description, isExpanded = false // Mặc định thu gọn
            )
        }
    }

    fun initFirstPokemon(id: Int) {
        if (idHistoryStack.isEmpty()) {
            idHistoryStack.add(id)
            xuLyId(id)
        }
    }

    fun xuLyId(id: Int) {
        if (currentPokemonId == id) return
        if (idHistoryStack.lastOrNull() != id) {
            idHistoryStack.add(id)
        }
        currentPokemonId = id
        fetchAllDataForPokemon(id)
    }

    private fun fetchAllDataForPokemon(pokemonId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isOfflineMode.postValue(false) //mặc định là có mạng
                val statsResponse = repository.getPokemonStats(pokemonId)
                if (statsResponse.isSuccessful && statsResponse.body() != null) {
                    val statsData = statsResponse.body()!!

                    _fragmentStats.postValue(statsData)

                    val newTypes = statsData.types.map { it.type.name }
                    val newImageUrl =
                        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${statsData.id}.png"
                    _detailPokemon.postValue(
                        DataPokemon(
                            statsData.id,
                            statsData.name,
                            newTypes,
                            newImageUrl
                        )
                    )

                    val primaryType = newTypes.firstOrNull() ?: "normal"
                    _bgColorId.postValue(evolutionMap.getColorId(primaryType))

                    // Dành cho Tab Moves
                    val moveUrls = statsData.moves.map { it.move.url }
                    moveTabPaginator.initUrls(moveUrls, viewModelScope)

                    val speciesUrl = statsData.species.url
                    val speciesResponse = repository.getPokemonSpeciesUrl(speciesUrl)
                    if (speciesResponse.isSuccessful && speciesResponse.body() != null) {
                        val speciesData = speciesResponse.body()!!
                        _pokemonSpeciesResponse.postValue(speciesData)

                        val evoUrl = speciesData.evolution_chain.url
                        fetchPokemonEvolution(evoUrl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isOfflineMode.postValue(true)
                val offlineData = db.getPokemonFromCacheById(pokemonId)
                offlineData?.let { data ->
                    _detailPokemon.postValue(data)

                    val primaryType = offlineData.types.firstOrNull() ?: "normal"
                    _bgColorId.postValue(evolutionMap.getColorId(primaryType))
                }
            }
        }
    }

    private suspend fun fetchPokemonEvolution(url: String) {
        val evolutionChain = repository.getPokemonEvolution(url)
        if (evolutionChain.isSuccessful && evolutionChain.body() != null) {
            val chain = evolutionChain.body()!!.chain
            val finalList = evolutionMap.mapToEvolutionItems(chain)
            _pokeEvolutionItem.postValue(finalList)
        }
    }

    suspend fun getMoveDetails(urls: List<String>): List<DataPokemonMove> {
        return coroutineScope {
            urls.map { url ->
                async {
                    try {
                        val response = repository.getMoveDetail(url)
                        if (response.isSuccessful && response.body() != null) {
                            val detail = response.body()!!

                            val cleanMove = detail.name.replace("-", " ")
                                .replaceFirstChar { it.uppercaseChar() }

                            val cleanPower = detail.power?.toString() ?: "-"
                            val cleanAccuracy = detail.accuracy?.toString() ?: "-"

                            val enFlavor =
                                detail.flavor_text_entries.find { it.language.name == "en" }
                            val description = enFlavor?.flavor_text?.replace("\n", " ")
                                ?: "No description available."

                            DataPokemonMove(
                                cleanMove,
                                detail.type.name,
                                detail.damage_class.name,
                                "Power: $cleanPower",
                                "Acc: $cleanAccuracy",
                                "PP: ${detail.pp}",
                                "Effect: $description"
                            )
                        } else null
                    } catch (e: Exception) {
                        Log.e("BUG", "Lỗi tải Move: ${e.message}")
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }
}