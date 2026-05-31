package com.example.pokedex.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.pokedex.R
import com.example.pokedex.data.model.EvolutionItem
import com.example.pokedex.databinding.ItemEvolutionBinding

class PokemonEvolutionChainAdapter(
    private val list: List<EvolutionItem>,
    private val onItemClick: (Int) -> Unit
) :
    RecyclerView.Adapter<PokemonEvolutionChainAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemEvolutionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val viTri = list[position]
        holder.binding.tvEvoName.text = viTri.name
        Glide.with(holder.itemView.context).load(viTri.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_loading)
            .error(R.drawable.ic_error).into(holder.binding.ivEvoPokemon)
        holder.binding.tvEvoCondition.text = viTri.condition
        holder.binding.tvEvoId.text = "#${viTri.id}"
        holder.itemView.setOnClickListener {
            onItemClick(viTri.id)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val binding: ItemEvolutionBinding) :
        RecyclerView.ViewHolder(binding.root)

}