package com.example.pokedex.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.data.model.DataPokemonMoveForSelection
import com.example.pokedex.databinding.ItemMoveSelectionBinding


class MoveSelectionAdapter(
    private val onChoose: (DataPokemonMoveForSelection) -> Unit
) : RecyclerView.Adapter<MoveSelectionAdapter.ViewHolder>() {
    private var expandedPosition = -1
    private var moves = listOf<DataPokemonMoveForSelection>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<DataPokemonMoveForSelection>) {
        moves = newList
        notifyDataSetChanged()
    }
    class ViewHolder(val binding: ItemMoveSelectionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMoveSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val moveName = moves[position]
        holder.binding.tvMoveNameSelect.text = moveName.name.replace("-", " ").uppercase()

        val isExpanded = position == expandedPosition
        holder.binding.tvMoveDetailSelect.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.binding.btnChooseMove.visibility = if (isExpanded) View.VISIBLE else View.GONE

        if (isExpanded) {
            holder.binding.tvMoveDetailSelect.text = "Loading description..."
            holder.binding.tvMoveDetailSelect.text = "Type: ${moveName.type} | ${moveName.damageClass} | \n ${moveName.power} | ${moveName.accuracy} | ${moveName.pp}\n ${moveName.description}"
        }
        holder.itemView.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos == RecyclerView.NO_POSITION) return@setOnClickListener

            val prevExpanded = expandedPosition
            expandedPosition = if (isExpanded) -1 else currentPos

            notifyItemChanged(prevExpanded)
            notifyItemChanged(currentPos)
        }

        holder.binding.btnChooseMove.setOnClickListener {
            onChoose(moveName)
        }
    }

    override fun getItemCount() = moves.size
}