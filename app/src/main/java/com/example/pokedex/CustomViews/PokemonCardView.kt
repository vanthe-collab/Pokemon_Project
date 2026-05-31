package com.example.pokedex.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.pokedex.R
import com.example.pokedex.databinding.ItemPokemonCustomviewBinding

class PokemonCardView : CardView {
    private lateinit var binding: ItemPokemonCustomviewBinding

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        binding = ItemPokemonCustomviewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    //hứng dữ liệu từ cardView Item_rv_pokemon
    fun setDataPokemon(
        iD: Int,
        name: String,
        listTypePokemon: List<String>,
        linkImage: String
    ) {
        binding.tvId.text = String.format("#%03d", iD)
        binding.tvName.text = name
        Glide.with(context).load(linkImage).into(binding.ivPokemon)
        if (listTypePokemon.isNotEmpty()) {
            binding.tvType1.text = listTypePokemon[0]
        }
        if (listTypePokemon.size > 1) {
            binding.tvType2.text = listTypePokemon[1]
            binding.tvType2.visibility = VISIBLE
        } else {
            //Không có hệ thứ 2, ẩn đi
            binding.tvType2.visibility = View.GONE
        }
        setColorBackGround(listTypePokemon)
    }

    fun setColorBackGround(typeList: List<String>) {
        val typePrimary = typeList[0]

        val colorResId = when (typePrimary.lowercase()) {
            "fire" -> R.color.poke_fire
            "water" -> R.color.poke_water
            "grass" -> R.color.poke_grass
            "electric" -> R.color.poke_electric
            "ice" -> R.color.poke_ice
            "fighting" -> R.color.poke_fighting
            "poison" -> R.color.poke_poison
            "ground" -> R.color.poke_ground
            "flying" -> R.color.poke_flying
            "psychic" -> R.color.poke_psychic
            "rock" -> R.color.poke_rock
            "bug" -> R.color.poke_bug
            "ghost" -> R.color.poke_ghost
            "dragon" -> R.color.poke_dragon
            "dark" -> R.color.poke_dark
            "steel" -> R.color.poke_steel
            "fairy" -> R.color.poke_fairy
            else -> R.color.poke_normal
        }

        // Đổi từ ID tài nguyên (colorResId) sang mã màu thật (ARGB)
        val mauThucTe = ContextCompat.getColor(context, colorResId)
        this.setCardBackgroundColor(mauThucTe)
    }
}