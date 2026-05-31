package com.example.pokedex.ui.activities

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.pokedex.data.repository.EvolutionMapper
import com.example.pokedex.data.model.DataPokemon
import com.example.pokedex.ui.adapters.PokemonPagerAdapter
import com.example.pokedex.databinding.ActivityDetailPokemonBinding
import com.example.pokedex.viewmodels.DetailViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.collections.isNotEmpty
import kotlin.let
import kotlin.text.replaceFirstChar
import kotlin.text.uppercase
import com.example.pokedex.R
import com.example.pokedex.data.Database.FavoriteDatabase
import com.example.pokedex.data.Database.TeamDatabase

class DetailPokemonActivity : AppCompatActivity() {
    private val evolutionMap = EvolutionMapper()
    private lateinit var binding: ActivityDetailPokemonBinding
    private var currentPokemon: DataPokemon? = null
    private val viewModel: DetailViewModel by viewModels()
    private val dbTeam = TeamDatabase(this)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU) // có thể xoa khi minSdk = 33 doi thanh 33
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPokemonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
        val pokemonId = intent.getIntExtra("poke_id", -1)
        // 2. Mồi lửa cho Bếp Trưởng đi chợ
        if (savedInstanceState == null) {
            viewModel.initFirstPokemon(pokemonId)
        }
        viewPager2AndTabLayouts(pokemonId)

        displayDataPokemon()

        // Xử lý sự kiện bấm nút Thêm vào Đội hình
        binding.btnAddTeam.setOnClickListener {
            // Kiểm tra xem currentPokemon có dữ liệu chưa (tránh lỗi crash app)
            currentPokemon?.let { pokemon ->

                val result = dbTeam.addPokemonToTeam(pokemon)

                if (result.first) {
                    // Thêm thành công
                    Toast.makeText(this, result.second, Toast.LENGTH_SHORT).show()
                } else {
                    // Thất bại (có thể do team đã đủ 6 con, hoặc con này đã có trong team rồi)
                    Toast.makeText(this, result.second, Toast.LENGTH_LONG).show()
                }

            } ?: run {
                // Trường hợp dữ liệu Pokemon chưa kịp load xong mà người dùng đã bấm nút
                Toast.makeText(this, "Đang tải dữ liệu, vui lòng thử lại sau!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Thay thế hoàn toàn FavoriteManager thành FavoriteDatabase
        binding.btnFavorite.setOnClickListener {
            currentPokemon?.let { pokemon ->
                val favoriteDb = FavoriteDatabase(this)

                if (favoriteDb.isFavorite(pokemon)) {
                    favoriteDb.removeFavorite(pokemon) // Nếu thích rồi thì xóa khỏi SQLite
                } else {
                    favoriteDb.addFavorite(pokemon) // Nếu chưa thích thì lưu vào SQLite
                }

                updateFavoriteIcon() // Cập nhật lại màu tim ngay lập tức
            }
        }
    }

    fun updateFavoriteIcon() {
        currentPokemon?.let { pokemon ->
            val favoriteDb = FavoriteDatabase(this)

            // Kiểm tra trạng thái từ database SQLite để set icon tương ứng
            if (favoriteDb.isFavorite(pokemon)) {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite)
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border)
            }
        }
    }

    // Load ảnh và hệ
    fun setUpImagesTypes(dataPokemon: DataPokemon, ivPokemon: ImageView) {
        Glide.with(this)
            .load(dataPokemon.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_loading)
            .error(R.drawable.ic_error)
            .into(ivPokemon)

        if (dataPokemon.types.isNotEmpty()) {
            val type1 = dataPokemon.types[0]
            binding.tvType1.text = type1.replaceFirstChar { it.uppercase() }
            binding.tvType1.visibility = View.VISIBLE

            val colorId1 = evolutionMap.getColorId(type1)
            val color1 = ContextCompat.getColor(this, colorId1)
            binding.tvType1.backgroundTintList = ColorStateList.valueOf(color1)
        }

        if (dataPokemon.types.size > 1) {
            val type2 = dataPokemon.types[1]
            binding.tvType2.text = type2.replaceFirstChar { it.uppercase() }
            binding.tvType2.visibility = View.VISIBLE

            // đổi màu hệ 2 (nếu có)
            val colorId2 = evolutionMap.getColorId(type2)
            val color2 = ContextCompat.getColor(this, colorId2)
            binding.tvType2.backgroundTintList = ColorStateList.valueOf(color2)
        } else {
            binding.tvType2.visibility = View.GONE
        }
    }

    // hiển thị các dữ liệu data Pokemon
    @RequiresApi(Build.VERSION_CODES.TIRAMISU) // có thể xoa khi minSdk = 33 doi thanh 33
    @SuppressLint("SetTextI18n")
    fun displayDataPokemon() {
        //kieerm tra co mang hay ko
        viewModel.isOfflineMode.observe(this) { isOffline ->
            if (isOffline) {
                binding.tabLayout.visibility = View.GONE
                binding.viewPager.visibility = View.GONE
                binding.layoutOfflineWarning.visibility = View.VISIBLE
            } else {
                binding.tabLayout.visibility = View.VISIBLE
                binding.viewPager.visibility = View.VISIBLE
                binding.layoutOfflineWarning.visibility = View.GONE
            }
        }
        //du mat hay co mang van goi ham nay
        viewModel.detailPokemon.observe(this) { data ->
            binding.tvDetailName.text = data.name
            binding.tvDetailId.text = "#${data.id}"
            setUpImagesTypes(data, binding.ivDetailPokemon)
            currentPokemon = data   // nhớ đang xem ai
            updateFavoriteIcon()    // set tim đúng màu
        }
        //doi mau nen
        viewModel.bgColorId.observe(this) { data ->
            val mauThucTe = ContextCompat.getColor(this, data)
            binding.detailRootLayout.setBackgroundColor(mauThucTe)
        }
    }

    fun viewPager2AndTabLayouts(pokemonId: Int) {
        val pagerAdapter = PokemonPagerAdapter(this, pokemonId)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            // đặt tên từng tab
            tab.text = when (position) {
                0 -> "About"
                1 -> "Base Stats"
                2 -> "Evolution"
                3 -> "Moves"
                else -> ""
            }
        }.attach()
    }
}