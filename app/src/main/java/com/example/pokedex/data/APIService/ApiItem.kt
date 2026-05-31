package com.example.pokedex.data.APIService

import com.example.pokedex.data.model.CategoryResponse
import com.example.pokedex.data.model.ItemDetailResponse
import com.example.pokedex.data.model.ItemNameUrlResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiItem {
    @GET("item")
    suspend fun getItemList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<ItemNameUrlResponse>

    @GET
    suspend fun getItemDetail(@Url url: String): Response<ItemDetailResponse>

    @GET("item-category/{category}/")
    suspend fun getItemsByCategory(
        @Path("category") category: String
    ): Response<CategoryResponse>

    @GET("berry/?limit=100") //Lấy tất cả loại berry
    suspend fun getAllBerries(): Response<ItemNameUrlResponse>
}