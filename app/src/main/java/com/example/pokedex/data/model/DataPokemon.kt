package com.example.pokedex.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

//Dành cho apdater
@Parcelize
data class DataPokemon(
    val id: Int,
    val name: String,
    val types: List<String>,
    val imageUrl: String,
): Parcelable


// Dành để hứng dữ liệu từ API
data class PokemonResponse(
    val results: List<PokemonListItem>
)

data class PokemonListItem(
    val name: String,
    val url: String
)


// 1. Json chứa chi tiết pokemon
data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val types: List<TypeSlot>, // Hứng mảng Hệ bị lồng sâu
    val sprites: Sprites       // Hứng mảng Ảnh bị lồng sâu
) {
    // Rút gọn link ảnh
    fun getOfficialImageUrl(): String {
        return sprites.other?.officialArtwork?.frontDefault ?: sprites.frontDefault
        ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/poke-ball.png"
    }

    //Biến mảng Object thành mảng chữ ["Fire", "Flying"]
    fun getSimpleTypes(): List<String> {
        return types.map { it.type.name }
    }
}

// CÁC CLASS PHỤ TRỢ ĐỂ BÓC TÁCH JSON LỒNG NHAU

// Dành cho việc tách ảnh
data class Sprites(
    @SerializedName("front_default") // đề phòng ảnh offical-art ko có trong API, chèn ảnh pixel vào
    val frontDefault: String?, val other: OtherSprites?
)

data class OtherSprites(@SerializedName("official-artwork") val officialArtwork: OfficialArtwork?)
data class OfficialArtwork(@SerializedName("front_default") val frontDefault: String?)

// Dành cho việc bóc tách Hệ
data class TypeSlot(val type: TypeDetail)
data class TypeDetail(val name: String)


//Dành cho việc lấy hp,def,attack,...
data class Stat(
    val name: String
)

data class BaseStat(
    val base_stat: Int,
    val stat: Stat
)

data class PokemonResponseStats(
    val id: Int,
    val stats: List<BaseStat>,
    val weight: Int,
    val height: Int,
    val abilities: List<SurroundingAbility>,
    val species: SpeciesInfo,
    val types: List<TypeSlot>,
    val name: String,
    val moves: List<moveList>
)

data class SurroundingAbility(
    val ability: ability
)

data class ability(
    val name: String
)
data class moveList(
    val move: moveInfor
)
data class moveInfor(
    val name: String,
    val url: String
)

//Dành cho việc lấy mô tả pokemon
data class PokemonSpeciesResponse(
    val id : Int,
    val base_happiness: Int,
    val evolution_chain: EvolutionChainUrl,
    val flavor_text_entries: List<FlavorText>,
    val genera: List<Genus>,
    val varieties: List<ItemEvolutionForm>
)

data class FlavorText(
    val flavor_text: String,
    val language: LanguageInfo
)

//Danh cho viec Lay giong/loai
data class Genus(
    val genus: String,
    val language: LanguageInfo
)

data class LanguageInfo(
    val name: String
)
data class ItemEvolutionForm(
    val is_default: Boolean,
    val pokemon: EvolutionForm
)
data class EvolutionForm(
    val name : String,
    val url: String
)

//danhf cho viec tien hoa
data class EvolutionChainUrl(
    val url: String
)

//dành cho việc lấy chain evolution
data class EvolutionChainResponse(
    val chain: ChainLink
)

data class ChainLink(
    val species: SpeciesInfo,
    val evolves_to: List<ChainLink>,
    val evolution_details: List<EvolutionDetail>
)

data class EvolutionDetail(
    val gender : Int?,
    val min_affection : Int?,
    val min_level: Int?,
    val min_beauty : Int?,
    val item: Item?,
    val min_damage_taken: Int?,
    val trigger: Trigger,
    val held_item: holderItem?,
    val min_happiness: Int?,
    val used_move: Move?,
    val min_move_count : Int?,
    val location: Location?,
    val base_form: BaseForm?,
    val time_of_day: String?,
    val region: Region?,
    val known_move: KnowMove?,
    val relative_physical_stats: Int?,
    val known_move_type: KnownMoveType?,
    val party_species: PartySpecies?,
    val trade_species: TradeSpecies?,
    val party_type : PartType?,
    val turn_upside_down: Boolean?,
    val needs_overworld_rain: Boolean?,
    val min_steps: Int?,
    val needs_multiplayer: Boolean,
)
data class holderItem(
    val name: String,
)
data class Item(
    val name: String
)

data class Trigger(
    val name: String
)
data class Move(
    val name : String
)
data class SpeciesInfo(
    val name: String,
    val url: String
)
data class Region(
    val name : String
)
data class Location(
    val name: String,
)

data class BaseForm(
    val name:String,
    val url: String,
)
data class KnowMove(
    val name: String,
)
data class KnownMoveType(
    val name: String
)

data class PartySpecies(
    val name: String
)
data class TradeSpecies(
    val name: String,
)
data class PartType(
    val name: String
)
//Adapter evolution Item
data class EvolutionItem(
    val name: String,
    val id: Int,
    val imageUrl: String,
    val condition: String,
    val lane: String,
)

//Class lấy moves
data class MoveDetailResponse(
    val id: Int,
    val name: String,
    val accuracy: Int?, // Dùng dấu ? vì có thể bị null
    val power: Int?,    // Dùng dấu ? vì có thể bị null
    val pp: Int,
    val type: MoveType,
    val damage_class: DamageClass,
    val flavor_text_entries: List<ListLanguageEng>
)

data class MoveType(val name: String)
data class DamageClass(val name: String)
data class ListLanguageEng(val language: LanguageInfor, val flavor_text: String)
data class LanguageInfor(val name: String)

//Class hien thi len UI
data class DataPokemonMove(
    val name: String,
    val type: String,
    val damageClass: String,
    val power: String,
    val accuracy: String,
    val pp: String,
    val description: String
)
data class DataPokemonMoveForSelection(
    val name: String,
    val type: String,
    val damageClass: String,
    val power: String,
    val accuracy: String,
    val pp: String,
    val description: String,
    val isExpanded: Boolean
)

