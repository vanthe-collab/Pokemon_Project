package com.example.pokedex.data.APIService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    const val BASE_URL = "https://pokeapi.co/api/v2/"
    val apiService: APIPokemon by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build().create(APIPokemon::class.java)
    }
    val apiItemService : ApiItem by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiItem::class.java)
    }

}