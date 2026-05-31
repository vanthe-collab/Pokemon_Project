package com.example.pokedex.ui.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.data.model.DataPokemonMove
import com.example.pokedex.databinding.ItemPokemonMoveBinding
import com.example.pokedex.data.repository.EvolutionMapper
import androidx.core.view.isVisible

class PokemonMoveAdapter: RecyclerView.Adapter<PokemonMoveAdapter.moveViewHolder>() {
    //Goi class EvolutionMap co chứa hàm đổi màu
    private val parseColor = EvolutionMapper()
    private var moveList = listOf<DataPokemonMove>()
    fun submitList(newList: List<DataPokemonMove>) {
        moveList = newList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): moveViewHolder {
        val binding = ItemPokemonMoveBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return moveViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: moveViewHolder,
        position: Int
    ) {
        holder.bind(moveList[position])
    }

    override fun getItemCount(): Int {
        return moveList.size
    }

    inner class moveViewHolder(val binding: ItemPokemonMoveBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(move: DataPokemonMove){
            binding.tvMoveName.text = move.name
            binding.tvMoveType.text = move.type.replaceFirstChar { it.uppercase() }
            binding.tvDamageClass.text = move.damageClass
            binding.tvPower.text = move.power
            binding.tvAccuracy.text = move.accuracy
            binding.tvPP.text = move.pp

            val type = move.type.lowercase()
            val colorTypeId = parseColor.getColorId(type)
            val colorType = ContextCompat.getColor(binding.root.context,colorTypeId)
            binding.tvMoveType.backgroundTintList = ColorStateList.valueOf(
                colorType)

            binding.tvMoveDescription.text = move.description
            //bắt sự kiện khi nhan Item
            binding.root.setOnClickListener {
                val isCurrentVisibility = binding.layoutDescription.isVisible
                if (isCurrentVisibility){
                    binding.layoutDescription.visibility = android.view.View.GONE
                }else{
                    binding.layoutDescription.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
}