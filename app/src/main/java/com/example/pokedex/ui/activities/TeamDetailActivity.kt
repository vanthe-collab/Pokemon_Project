package com.example.pokedex.ui.activities

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.pokedex.R
import com.example.pokedex.data.model.DataPokemon
import com.example.pokedex.data.Database.TeamDatabase
import com.example.pokedex.databinding.ActivityTeamDetailBinding
import com.example.pokedex.viewmodels.DetailViewModel
import kotlin.getValue

class TeamDetailActivity : AppCompatActivity() {

    val viewModel: DetailViewModel by viewModels()
    private lateinit var binding: ActivityTeamDetailBinding
    private lateinit var db: TeamDatabase
    private var pokemon: DataPokemon? = null


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = TeamDatabase(this)

        //Nhan data Pokemon
        val receivedPokemon = intent.getParcelableExtra<DataPokemon>("Poke_data")
        if (receivedPokemon != null) {
            this.pokemon = receivedPokemon
            binding.tvNameDetailTeam.text = receivedPokemon.name
            Glide.with(this).load(receivedPokemon.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_error)
                .into(binding.ivDetailTeam)
        } else {
            Toast.makeText(this, "Error: Can't get Pokemon data!", Toast.LENGTH_SHORT).show()
        }

        binding.btnBack.setOnClickListener { finish() }

        viewModel.fragmentStats.observe(this) { data ->

            val moveUrls = data.moves.map { it.move.url }
            viewModel.selectionPaginator.initUrls(moveUrls, lifecycleScope)
            // Đổ Stats lên giao diện
            val mapList = data.stats.associate { it.stat.name to it.base_stat }
            val hpValue = mapList["hp"] ?: 0
            val attackValue = mapList["attack"] ?: 0
            val defValue = mapList["defense"] ?: 0
            val spaValue = mapList["special-attack"] ?: 0
            val spdValue = mapList["special-defense"] ?: 0
            val spValue = mapList["speed"] ?: 0
            val total = mapList.values.sum()

            binding.tvStatHP.text = hpValue.toString()
            binding.tvStatAT.text = attackValue.toString()
            binding.tvStatDef.text = defValue.toString()
            binding.tvStatSPAtk.text = spaValue.toString()
            binding.tvStatSPDef.text = spdValue.toString()
            binding.tvStatSpeed.text = spValue.toString()
            binding.tvStatTotal.text = total.toString()

            setEffectProgressBar(binding.pbStatHP, hpValue)
            setEffectProgressBar(binding.pbStatAT, attackValue)
            setEffectProgressBar(binding.pbStatDef, defValue)
            setEffectProgressBar(binding.pbStatSPAtk, spaValue)
            setEffectProgressBar(binding.pbStatSPDef, spdValue)
            setEffectProgressBar(binding.pbStatSpeed, spValue)
            // Đọc dữ liệu cũ từ SQLite và điền chữ đẹp lên 4 Slot

            pokemon?.let { poke ->
                val savedMoves = db.getSavedMoves(poke.id)
                if (savedMoves.size == 4) {
                    binding.tvMoveSlot1.text = savedMoves[0].replace("-", " ").uppercase()
                    binding.tvMoveSlot2.text = savedMoves[1].replace("-", " ").uppercase()
                    binding.tvMoveSlot3.text = savedMoves[2].replace("-", " ").uppercase()
                    binding.tvMoveSlot4.text = savedMoves[3].replace("-", " ").uppercase()
                }
            }

        }

        pokemon?.let { pokemon ->
            viewModel.xuLyId(pokemon.id)
        }

        // Đăng ký nút bấm
        binding.tvMoveSlot1.setOnClickListener { openMoveSelectionBottomSheet(1) }
        binding.tvMoveSlot2.setOnClickListener { openMoveSelectionBottomSheet(2) }
        binding.tvMoveSlot3.setOnClickListener { openMoveSelectionBottomSheet(3) }
        binding.tvMoveSlot4.setOnClickListener { openMoveSelectionBottomSheet(4) }

        // Xử lý nút lưu
        binding.btnSaveTeamDetail.setOnClickListener {
            this@TeamDetailActivity.pokemon?.let {
                // Đổi ngược chuỗi hiển thị có khoảng trắng chữ hoa về dạng gạch nối nguyên bản để lưu SQLite
                val movesToSave = listOf(
                    binding.tvMoveSlot1.text.toString().toRawMoveName(),
                    binding.tvMoveSlot2.text.toString().toRawMoveName(),
                    binding.tvMoveSlot3.text.toString().toRawMoveName(),
                    binding.tvMoveSlot4.text.toString().toRawMoveName()
                )
                val isSaved = db.updatePokemonMoves(it.id, movesToSave)
                if (isSaved) {
                    Toast.makeText(this, "Save succeeded!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openMoveSelectionBottomSheet(slotNumber: Int) {
        val bottomSheet = MovesBottomSheet { selectedMove ->
            val formattedName = selectedMove.name.replace("-", " ").uppercase()
            when (slotNumber) {
                1 -> binding.tvMoveSlot1.text = formattedName
                2 -> binding.tvMoveSlot2.text = formattedName
                3 -> binding.tvMoveSlot3.text = formattedName
                4 -> binding.tvMoveSlot4.text = formattedName
            }
        }
        bottomSheet.show(supportFragmentManager, "MovesBottomSheet")
    }

    private fun String.toRawMoveName(): String {
        if (this == "---") return "---"
        return this.lowercase().replace(" ", "-")
    }

    private fun setEffectProgressBar(progressBar: ProgressBar, value: Int) {
        val colorHex = when {
            value <= 100 -> "#FFA726"
            value in 101..150 -> "#4CAF50"
            else -> "#00897B"
        }
        val colorInt = colorHex.toColorInt()
        progressBar.progressTintList = ColorStateList.valueOf(colorInt)
        ObjectAnimator.ofInt(progressBar, "progress", 0, value).setDuration(1000).start()
    }
}