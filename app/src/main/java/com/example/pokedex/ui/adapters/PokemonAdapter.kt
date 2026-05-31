package com.example.pokedex.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.data.model.DataPokemon
import com.example.pokedex.ui.activities.DetailPokemonActivity
import com.example.pokedex.databinding.ItemPokemonDapterBinding

class PokemonAdapter(private var list: List<DataPokemon>) :
    RecyclerView.Adapter<PokemonAdapter.ViewHolder>() {

    private var pokemonListFull: List<DataPokemon> = ArrayList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPokemonDapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viTri = list[position]

        // Kiểm tra dữ liệu an toàn để chặn đứng lỗi văng App khi gõ tìm kiếm
        val safeTypes = if (viTri.types.isNotEmpty()) {
            viTri.types.map { it.replaceFirstChar { it.uppercase() } }
        } else {
            // Nếu mảng hệ bị rỗng (do đang tìm kiếm toàn cục chưa kịp load),
            // truyền vào một danh sách tạm thời để Custom View không bị crash.
            listOf("...")
        }

        // Đổ dữ liệu an toàn vào Custom View của bạn
        holder.binding.pokemonCardView.setDataPokemon(
            viTri.id,
            viTri.name.replace("-"," "),
            safeTypes, // Sử dụng biến types an toàn đã qua xử lý
            viTri.imageUrl
        )

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailPokemonActivity::class.java)
            intent.putExtra("poke_id", viTri.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size

    // Cập nhật khi cuộn loadmore trang bình thường
    fun updateData(newList: List<DataPokemon>) {
        this.list = newList
        this.pokemonListFull = ArrayList(newList)
        notifyDataSetChanged()
    }

    // Cập nhật danh sách hiển thị khi gõ tìm kiếm toàn cục
    fun filterList(filteredList: List<DataPokemon>) {
        this.list = filteredList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemPokemonDapterBinding) : RecyclerView.ViewHolder(binding.root)
}