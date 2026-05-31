package com.example.pokedex.data.APIService

import com.example.pokedex.data.model.EvolutionChainResponse
import com.example.pokedex.data.model.MoveDetailResponse
import com.example.pokedex.data.model.PokemonDetailResponse
import com.example.pokedex.data.model.PokemonResponse
import com.example.pokedex.data.model.PokemonResponseStats
import com.example.pokedex.data.model.PokemonSpeciesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface APIPokemon {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<PokemonResponse>

    //dùng để lấy url từ pokemon chi tiết
    @GET
    suspend fun getPokemonDetail(@Url url: String): Response<PokemonDetailResponse>

    @GET("pokemon/{id}")
    suspend fun getPokemonStats(
        @Path("id") id: Int
    ): Response<PokemonResponseStats>

    @GET
    suspend fun getEvolutionChain(
        @Url url: String
    ): Response<EvolutionChainResponse>


    @GET
    suspend fun getPokemonSpeciesByUrl(@Url url: String): Response<PokemonSpeciesResponse>

    //ham lay api move
    @GET
    suspend fun getMoveDetail(@Url url: String): Response<MoveDetailResponse>
}