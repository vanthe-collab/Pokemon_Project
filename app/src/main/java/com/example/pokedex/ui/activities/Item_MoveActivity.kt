package com.example.pokedex.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.ui.adapters.ItemInventoryAdapter
import com.example.pokedex.databinding.ActivityItemMoveBinding
import com.example.pokedex.data.model.ItemData
import com.example.pokedex.viewmodels.ItemViewModel
import androidx.core.graphics.toColorInt

class Item_MoveActivity : AppCompatActivity() {
    private val viewModel: ItemViewModel by viewModels()

    private var listSumary = ArrayList<ItemData>()
    private lateinit var binding: ActivityItemMoveBinding

    private lateinit var adapter: ItemInventoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemMoveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.selectedItemId = R.id.nav_move_items
        binding.bottomNavigation.itemIconTintList = null

        adapter = ItemInventoryAdapter(listSumary)
        binding.rvItems.layoutManager = GridLayoutManager(
            this, 2,
            GridLayoutManager.VERTICAL, false
        )
        binding.rvItems.adapter = adapter
        observeViewModel()
        setupBottomNavigation()
        scrollDown()
        if (viewModel.dataItem.value.isNullOrEmpty()) {
            viewModel.selectCategory("All")
        }
        selectedCategory()
    }

    //gọi API lấy Item
    @SuppressLint("NotifyDataSetChanged")
    fun observeViewModel() {
        viewModel.dataItem.observe(this) { data ->
            //dọn sạch list hiện tại
            listSumary.clear()

            //thêm data vào
            listSumary.addAll(data)

            adapter.notifyDataSetChanged()
        }
    }

    fun scrollDown() {
        binding.rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { //kiem tra co cuon xuong hay ko
                    val layoutManger = recyclerView.layoutManager as GridLayoutManager
                    val visibleItemCount = layoutManger.childCount
                    val totalItemCount = layoutManger.itemCount
                    val pasVisibleItems = layoutManger.findFirstVisibleItemPosition()
                    binding.bottomNavigation.visibility = View.GONE
                    if (!viewModel.isLoading) {
                        if ((visibleItemCount + pasVisibleItems) >= totalItemCount) {
                            viewModel.onScrollToBottom()
                        }
                    }
                } else {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        })
    }

    fun setupBottomNavigation() {

        binding.bottomNavigation.selectedItemId = R.id.nav_move_items

        binding.bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                // qua pokedex
                R.id.nav_pokedex -> {

                    val intent = Intent(this, MainActivity::class.java)

                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP

                    startActivity(intent)

                    overridePendingTransition(0, 0)

                    finish()

                    true
                }

                // đang ở item
                R.id.nav_move_items -> {
                    true
                }

                // qua favorite
                R.id.nav_favorite -> {

                    val intent = Intent(this, FavoriteActivity::class.java)

                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP

                    startActivity(intent)

                    overridePendingTransition(0, 0)

                    finish()

                    true
                }

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

    //hàm bắt sự kiện các chip lọc
    fun selectedCategory() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            // không chọn nút nào thì không làm gì cả
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val checkedId = checkedIds.first()

            updateChipColors(checkedId)
            // Kiểm tra xem user đang bấm nút nào
            when (checkedId) {
                R.id.chipAll -> {
                    viewModel.selectCategory("All")
                }

                R.id.chipPokeballs -> {
                    viewModel.selectCategory("standard-balls")
                }

                R.id.chipHealing -> {
                    viewModel.selectCategory("healing")
                }

                R.id.chipTM_HM -> {
                    viewModel.selectCategory("all-machines")
                }

                R.id.chipBerry -> {
                    viewModel.selectCategory("berries")
                }
                // Thêm các nút khác nếu thích.....
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.menu.findItem(R.id.nav_move_items).isChecked = true
    }

    private fun updateChipColors(checkedId: Int) {
        //quét tất cả các chip
        for (i in 0 until binding.chipGroupFilter.childCount) {
            val chip =
                binding.chipGroupFilter.getChildAt(i) as com.google.android.material.chip.Chip
            // Màu xám đen cho nền
            chip.chipBackgroundColor =
                android.content.res.ColorStateList.valueOf("#E8E2EC".toColorInt())
            // Chữ màu xám nhạt
            chip.setTextColor("#4A4458".toColorInt())
        }
        val selectedChip =
            binding.chipGroupFilter.findViewById<com.google.android.material.chip.Chip>(checkedId)
        if (selectedChip != null) {
            val colorHex = when (checkedId) {
                R.id.chipAll -> "#4287f5"         // Xanh dương All
                R.id.chipPokeballs -> "#f54242"   // Đỏ rực Pokeball
                R.id.chipHealing -> "#42f575"     // Xanh lá Thuốc
                R.id.chipBerry -> "#f5a442"     // Cam đất Trái cây
                R.id.chipTM_HM -> "#f5d142"       // Vàng đồng TM
                else -> "#4287f5"
            }
            // Sơn nền màu mới
            selectedChip.chipBackgroundColor =
                android.content.res.ColorStateList.valueOf(colorHex.toColorInt())
            // Sửa chữ thành màu Trắng cho dễ đọc
            selectedChip.setTextColor(android.graphics.Color.WHITE)
        }
    }
}