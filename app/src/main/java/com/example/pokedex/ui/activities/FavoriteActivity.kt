package com.example.pokedex.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pokedex.R
import com.example.pokedex.data.Database.FavoriteDatabase // 🔥 Import đúng file database mới trong model
import com.example.pokedex.databinding.ActivityFavoriteBinding
import com.example.pokedex.ui.adapters.PokemonAdapter

class FavoriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var adapter: PokemonAdapter
    private lateinit var favoriteDb: FavoriteDatabase // 🔥 Khai báo biến toàn cục cho SQLite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔥 Khởi tạo database SQLite
        favoriteDb = FavoriteDatabase(this)

        // set icon bottom nav giữ màu gốc
        binding.bottomNavigation.itemIconTintList = null

        setupRecycler()
        setupBottomNavigation()

        loadFavorites()
    }

    // setup recyclerview
    private fun setupRecycler() {
        binding.rvFavoriteList.layoutManager = GridLayoutManager(this, 2)
        adapter = PokemonAdapter(arrayListOf())
        binding.rvFavoriteList.adapter = adapter
    }

    // setup bottom navigation
    private fun setupBottomNavigation() {
        // chọn tab favorite
        binding.bottomNavigation.selectedItemId = R.id.nav_favorite

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // qua pokedex
                R.id.nav_pokedex -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish() // 🔥 Đóng màn hình Favorite để giải phóng RAM, tránh lỗi lặp màn hình
                    true
                }

                // qua item
                R.id.nav_move_items -> {
                    startActivity(Intent(this, Item_MoveActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish() // 🔥 Đóng màn hình Favorite
                    true
                }

                // đang ở favorite
                R.id.nav_favorite -> true

                R.id.nav_team -> {
                    startActivity(Intent(this, TeamActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                else -> false
            }
        }
    }

    // 🔥 LOAD DANH SÁCH FAVORITE TỪ DATABASE SQLITE
    private fun loadFavorites() {
        // Gọi hàm lấy danh sách offline thay thế hoàn toàn cho FavoriteManager cũ
        val list = favoriteDb.getFavorites()

        // chưa có pokemon favorite
        if (list.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvFavoriteList.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvFavoriteList.visibility = View.VISIBLE

            adapter = PokemonAdapter(ArrayList(list))
            binding.rvFavoriteList.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()

        // load lại khi quay về màn hình (để cập nhật ngay nếu vừa bỏ thích ở màn Detail)
        loadFavorites()

        // giữ icon favorite được chọn
        binding.bottomNavigation.menu
            .findItem(R.id.nav_favorite)
            .isChecked = true
    }
}