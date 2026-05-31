package com.example.pokedex.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.data.Database.TeamDatabase
import com.example.pokedex.databinding.ActivityTeamBinding
import com.example.pokedex.ui.adapters.TeamAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.jvm.java

class TeamActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeamBinding
    private lateinit var db: TeamDatabase
    private lateinit var adapter: TeamAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Database
        db = TeamDatabase(this)

        // Xử lý sự kiện nhấn nút Explore Pokemon
        binding.btnExplorePokemon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0) // Loại bỏ hiệu ứng giật màn hình
            finish()
        }

        setupRecyclerView()
        loadTeamData()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        // Hiển thị dạng lưới 2 cột
        binding.rvMyTeam.layoutManager = GridLayoutManager(this, 2)
        adapter = TeamAdapter(mutableListOf()) { pokemon ->
            // Logic xóa Pokemon khi adapter gọi callback
            val isDeleted = db.removePokemonFromTeam(pokemon.id)
            if (isDeleted) {
                Toast.makeText(this, "Deleted ${pokemon.name}", Toast.LENGTH_SHORT).show()
                loadTeamData() // Load lại danh sách sau khi xóa
            }
        }
        binding.rvMyTeam.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun loadTeamData() {
        val teamList = db.getTeam()
        val count = teamList.size

        binding.tvTeamCount.text = "MY TEAM ($count/6)"

        if (teamList.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvMyTeam.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvMyTeam.visibility = View.VISIBLE
            adapter.updateData(teamList)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationTeam.itemIconTintList = null

        binding.bottomNavigationTeam.selectedItemId = R.id.nav_team
        binding.bottomNavigationTeam.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_pokedex -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_favorite -> {
                    startActivity(Intent(this, FavoriteActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_move_items -> {
                    startActivity(Intent(this, Item_MoveActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}