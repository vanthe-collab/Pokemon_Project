package com.example.pokedex.data.model

data class ItemData(
    val imageUrl: String,
    val itemName: String,
    val itemIntroduce: String,
    val effectItem: String,
    val categoryCost : String
)
//List của nó
data class ItemNameUrlResponse(
    val results: List<ItemNameDescriptionUrl>
)

//lấy tên và url chi tiet cua Item
data class ItemNameDescriptionUrl(
    val name: String,
    val url: String
)

//List Category
data class CategoryResponse(
    val items: List<ItemReference>
)

data class ItemReference(
    val name: String,
    val url: String
)

data class ItemDetailResponse(
    val cost : Int,
    val category: Category,
    val name: String,
    val sprites: ImageItem,
    val flavor_text_entries: List<FlavorTextEntry>,
    val effect_entries: List<EffectEntry>
) {
    fun getOfficialImageUrl(): String {
        return sprites.default
    }
}
data class ImageItem(
    val default: String
)

// GIỚI THIỆU ITEM
data class FlavorTextEntry(
    val language: Language,
    val text: String
)

data class Language(
    val name: String,
)


//CHỨC NĂNG ITEM
data class EffectEntry(
    val effect : String,
    val language: LanguageEffect
)
data class LanguageEffect(
    val name: String
)
//Category
data class Category(
    val name: String
)