package com.example.pokedex.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.Database.ItemCacheDatabase
import com.example.pokedex.data.model.Category
import com.example.pokedex.data.model.ItemData
import com.example.pokedex.data.repository.PokemonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.replace

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val db = ItemCacheDatabase(application)
    private val repository = PokemonRepository()
    private var limit = 20
    private var currentOffset = 0

    //Kho đồ (lưu tất cả những gì đã tải)
    private var totalList = ArrayList<ItemData>()
    var isLoading = false

    //Data Item
    private val _dataItem = MutableLiveData<List<ItemData>>()
    val dataItem: LiveData<List<ItemData>> = _dataItem

    //Danh sach chua cac link urls da lay
    private var currentFilterCategory = "All"
    private var currentFilterUrls = listOf<String>()
    private var filterOffset = 0 //danh dau vi tri

    //xay dung ham ket noi api Item
    fun fetchAllItems() {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            // Bước 1: Hỏi thăm Kho Local xem có đồ không?
            val cachedItems = db.getAllCache(limit, currentOffset)

            if (cachedItems.isNotEmpty()) {
                // TRƯỜNG HỢP 1: ĐÃ CÓ TRONG KHO -> LẤY XÀI LUÔN, KHÔNG GỌI MẠNG
                withContext(Dispatchers.Main) {
                    totalList.addAll(cachedItems)
                    _dataItem.value = ArrayList(totalList)
                    currentOffset += cachedItems.size
                    isLoading = false
                }
            } else {
                // TRƯỜNG HỢP 2: KHO TRỐNG -> ĐI XIN POKEAPI
                val response = repository.getItemList(limit, currentOffset)
                if (response.isSuccessful && response.body() != null) {
                    val urls = response.body()!!.results.map { it.url }
                    // Truyền thêm mác "All" để cất vào kho
                    fetchDetailsForUrls(urls, "All")
                } else {
                    isLoading = false
                }
            }
        }
    }

    //Tách hàm async ra 1 bên
    private suspend fun fetchDetailsForUrls(rawItems: List<String>, categoryName: String) {
        val listDataFormat = coroutineScope {
            rawItems.map { url ->
                async {
                    try {
                        val detailItemResponse = repository.getItemListDetail(url)
                        if (detailItemResponse.isSuccessful && detailItemResponse.body() != null) {
                            val detail = detailItemResponse.body()!!
                            val introduce = detail.flavor_text_entries.find { it.language.name == "en" }?.text?.replace("\n", " ") ?: " "
                            val effect = detail.effect_entries.find { it.language.name == "en" }?.effect?.replace("\n", " ")?.replace("  ", " ") ?: " "
                            val categoryCost = "${detail.category.name} - ${detail.cost} ₽"

                            ItemData(
                                detail.getOfficialImageUrl(),
                                detail.name.replace("-", " "),
                                introduce,
                                effect,
                                categoryCost
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.d("BUG_KO_TAI_DUOC", "Lỗi tải món đồ: ${e.message}")
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

        // CẤT ĐỒ VÀO DATABASE LOCAL
        if (listDataFormat.isNotEmpty()) {
            db.insertCache(listDataFormat, categoryName)
        }

        // Cập nhật lên màn hình
        withContext(Dispatchers.Main) {
            if (listDataFormat.isNotEmpty()) {
                updateUIList(listDataFormat)
            }
            if (categoryName == "All") {
                currentOffset += rawItems.size
            } else {
                filterOffset += rawItems.size
            }
            isLoading = false
        }
    }


    //hàm gọi API để lọc Items
    private fun fetchItemByCategory(category: String) {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getItemsByCategory(category)
                if (response.isSuccessful && response.body() != null) {
                    val urls = response.body()!!.items.map { it.url }
                    currentFilterUrls = urls
                    filterOffset = 0
                    loadNextChunk()
                } else {
                    isLoading = false
                }
            }catch (e: Exception){
                Log.e("OFFLINE", "Mất mạng chuyển sang database tab $category")
                val offlineMode = db.getCacheByCategory(category,999,0)
                withContext(Dispatchers.Main){
                    if (offlineMode.isNotEmpty()){
                        updateUIList(offlineMode)
                    }
                    isLoading = false
                }
            }

        }
    }

    //
    fun selectCategory(category: String) {
        if (isLoading) return

        // 1. Đổi Tab -> Dọn sạch list cũ ngay lập tức!
        totalList.clear()
        _dataItem.value = emptyList()
        currentFilterCategory = category

        when (category) {
            "All" -> {
                currentOffset = 0
                fetchAllItems()
            }
            "berries" -> fetchBerries()
            else -> fetchItemByCategory(category)
        }
    }

    private fun fetchBerries() {
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getAllBerries()
                if (response.isSuccessful && response.body() != null) {
                    val urls = response.body()!!.results.map { berry ->
                        "https://pokeapi.co/api/v2/item/${berry.name}-berry/"
                    }
                    currentFilterUrls = urls
                    filterOffset = 0
                    loadNextChunk()
                } else {
                    isLoading = false
                }
            }catch (e: Exception){
                Log.e("OFFLINE", "Mất mạng chuyển sang database tab berries")
                val offlineMode = db.getCacheByCategory("berries",999,0)
                withContext(Dispatchers.Main){
                    if (offlineMode.isNotEmpty()){
                        updateUIList(offlineMode)
                    }
                    isLoading = false
                }
            }

        }
    }

    private fun loadNextChunk() {
        if (filterOffset >= currentFilterUrls.size) {
            isLoading = false
            return
        }

        // Khóa cửa chống vuốt nhanh
        isLoading = true

        viewModelScope.launch(Dispatchers.IO) {
            val cachedItems = db.getCacheByCategory(currentFilterCategory, limit, filterOffset)

            if (cachedItems.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    updateUIList(cachedItems)
                    filterOffset += cachedItems.size
                    isLoading = false
                }
            } else {
                val chunkUrls = currentFilterUrls.drop(filterOffset).take(limit)
                fetchDetailsForUrls(chunkUrls, currentFilterCategory)
            }
        }
    }

    fun onScrollToBottom() {
        if (isLoading) return
        if (currentFilterCategory == "All") {
            fetchAllItems()
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                loadNextChunk()
            }
        }
    }
    private fun updateUIList(newItems: List<ItemData>) {
        totalList.addAll(newItems)
        // Quét qua toàn bộ danh sách, item nào trùng Tên thì xóa bớt!
        val uniqueList = totalList.distinctBy { it.itemName }

        totalList.clear()
        totalList.addAll(uniqueList)
        _dataItem.value = ArrayList(totalList)
    }
}