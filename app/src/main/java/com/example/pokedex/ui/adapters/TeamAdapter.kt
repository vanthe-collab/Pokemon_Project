package com.example.pokedex.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.pokedex.R
import com.example.pokedex.data.model.DataPokemon
import com.example.pokedex.data.repository.EvolutionMapper // Gọi EvolutionMapper của bạn vào đây
import com.example.pokedex.databinding.ItemTeamPokemonBinding
import com.example.pokedex.ui.activities.TeamDetailActivity
import com.google.gson.Gson
import kotlin.apply
import kotlin.collections.firstOrNull
import kotlin.jvm.java
import kotlin.text.lowercase

class TeamAdapter(
    private var teamList: MutableList<DataPokemon>,
    private val onRemoveClick: (DataPokemon) -> Unit
) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    // Khởi tạo EvolutionMapper một lần duy nhất để tái sử dụng
    private val evolutionMapper = EvolutionMapper()

    class TeamViewHolder(val binding: ItemTeamPokemonBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val binding = ItemTeamPokemonBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TeamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val pokemon = teamList[position]
        val context = holder.itemView.context
        holder.binding.tvPokemonNameTeam.text = pokemon.name

        // Load ảnh Pokemon
        Glide.with(context)
            .load(pokemon.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_loading)
            .error(R.drawable.ic_error)
            .into(holder.binding.ivPokemonTeam)

        // XỬ LÝ ĐỔI MÀU NỀN THEO HỆ BẰNG EVOLUTION MAPPER CỦA BẠN
        val pokemonType = pokemon.types.firstOrNull() // Lấy hệ đầu tiên

        if (pokemonType != null) {
            // Lấy ID màu từ class EvolutionMapper
            val colorResId = evolutionMapper.getColorId(pokemonType.lowercase())
            // Chuyển ID màu thành màu thực tế của Android
            val color = ContextCompat.getColor(context, colorResId)
            // Đổ màu vào nền CardView
            holder.binding.cardViewTeamPokemon.setCardBackgroundColor(color)
        } else {
            // Nếu lỗi không có hệ, set màu mặc định (ví dụ poke_normal)
            holder.binding.cardViewTeamPokemon.setCardBackgroundColor(ContextCompat.getColor(context, R.color.poke_normal))
        }

        // Xử lý sự kiện nút xóa
        holder.binding.btnRemove.setOnClickListener {
            onRemoveClick(pokemon)
        }

        //  khi click vào thẻ Pokemon
        holder.itemView.setOnClickListener {
            val intent = Intent(context, TeamDetailActivity::class.java).apply {
               putExtra("Poke_data",pokemon)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = teamList.size

    fun updateData(newList: List<DataPokemon>) {
        teamList.clear()
        teamList.addAll(newList)
        notifyDataSetChanged()
    }
}