package com.example.pokedex.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokedex.databinding.ItemInventoryBinding
import com.example.pokedex.databinding.LayoutBottomSheetItemBinding
import com.example.pokedex.data.model.ItemData
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.core.graphics.toColorInt

class ItemInventoryAdapter(private val list: List<ItemData>) :
    RecyclerView.Adapter<ItemInventoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viTri = list[position]
        holder.binding.tvItemName.text = viTri.itemName.replaceFirstChar { it.uppercase() }.replace("-"," ")
        holder.binding.tvItemIntroduce.text = viTri.itemIntroduce
        Glide.with(holder.itemView.context).load(viTri.imageUrl).into(holder.binding.ivItemImage)

        //gắn sự kiện cho bottom sheet
        holder.itemView.setOnClickListener {
            val bottomSheet = BottomSheetDialog(holder.itemView.context)

           val bindingBottomSheet =
                LayoutBottomSheetItemBinding.inflate(LayoutInflater.from(holder.itemView.context))
            bottomSheet.setContentView(bindingBottomSheet.root)

            bindingBottomSheet.tvBsItemName.text = viTri.itemName.replaceFirstChar { it.uppercase() }
            bindingBottomSheet.tvBsItemCategoryCost.text = viTri.categoryCost
            bindingBottomSheet.tvBsItemEffect.text = viTri.effectItem
            Glide.with(holder.itemView.context).load(viTri.imageUrl)
                .into(bindingBottomSheet.ivBsItemImage)

            bottomSheet.show()
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val binding: ItemInventoryBinding) :
        RecyclerView.ViewHolder(binding.root)

}