package com.example.pokedex.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.data.model.DataPokemon
import com.example.pokedex.data.model.PokemonListItem
import com.example.pokedex.ui.adapters.PokemonAdapter
import com.example.pokedex.databinding.ActivityMainBinding
import com.example.pokedex.viewmodels.PokeDexViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.core.view.isVisible
import androidx.core.view.isGone
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pokedex.databinding.ItemFriendBinding
import com.example.pokedex.databinding.SearchFriendLayoutBinding
import com.example.pokedex.ui.adapters.FriendAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.emptyList

class MainActivity : AppCompatActivity() {

    private val sharedPreferences by lazy {
        getSharedPreferences(
            "PokedexPrefs",
            Context.MODE_PRIVATE
        )
    }
    private lateinit var binding: ActivityMainBinding
    private val viewModel: PokeDexViewModel by viewModels()
    private var listSumary = ArrayList<DataPokemon>() // Danh sách phân trang bình thường
    private var allGlobalPokemonNames =
        ArrayList<PokemonListItem>() // Danh sách tổng > 1000 con tải riêng
    private lateinit var adapter: PokemonAdapter
    private var bottomNavVisibility = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.itemIconTintList = null

        adapter = PokemonAdapter(listSumary)
        binding.rvPokemonList.layoutManager =
            GridLayoutManager(this@MainActivity, 2, GridLayoutManager.VERTICAL, false)
        binding.rvPokemonList.adapter = adapter

        obseverViewModel()
        scrollDown()
        setupBottomNavigation()
        setupNavigationDrawer()
        setupSearchAndLogout()
        displayTrainerNavigation()
        // 1. Gọi API phân trang cũ qua ViewModel
        if (viewModel.pokeList.value.isNullOrEmpty()) {
            viewModel.fetchPokemonList()
        }

        // 2. TỰ GỌI API LẤY DANH SÁCH TỔNG NGAY TRONG MAIN (Lấy đúng từ nguồn gốc pokeapi.co)
        fetchGlobalPokemonDirectly()

        // ĐĂNG KÝ BỘ XỬ LÝ NÚT BACK CHUẨN ANDROIDX
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.searchView.isVisible) {
                    closeSearchView()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }
    //ham hien thong tin trainer

    fun displayTrainerNavigation() {
        //set ten trainer cho navigation
        val currentUsername = sharedPreferences.getString("currentUsername", "Trainer")
        val headerView = binding.navigationView.getHeaderView(0)
        val tvName = headerView.findViewById<TextView>(R.id.tvName)
        val image = headerView.findViewById<ImageView>(R.id.imgAvatar)
        tvName.text = currentUsername
        image.setImageResource(R.drawable.icon_pikachu)

    }

    // HÀM TỰ CHẠY MẠNG NGẦM TẢI TÊN VÀ URL CỦA 1300 CON POKEMON TỪ API GỐC
    private fun fetchGlobalPokemonDirectly() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://pokeapi.co/api/v2/pokemon?limit=1300")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(jsonString)
                    val resultsArray = jsonObject.getJSONArray("results")

                    val tempList = ArrayList<PokemonListItem>()
                    for (i in 0 until resultsArray.length()) {
                        val item = resultsArray.getJSONObject(i)
                        tempList.add(
                            PokemonListItem(
                                name = item.getString("name"),
                                url = item.getString("url")
                            )
                        )
                    }

                    // Tải xong đưa kết quả về Main Thread để dùng
                    withContext(Dispatchers.Main) {
                        allGlobalPokemonNames.clear()
                        allGlobalPokemonNames.addAll(tempList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun closeSearchView() {
        binding.tvToolbarTitle.visibility = View.VISIBLE
        binding.searchView.visibility = View.GONE
        binding.searchView.setQuery("", false)

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)

        adapter.filterList(listSumary)
    }

    private fun setupSearchAndLogout() {
        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.fab.setOnClickListener {
            if (binding.searchView.isGone) {
                binding.tvToolbarTitle.visibility = View.GONE
                binding.searchView.visibility = View.VISIBLE
                binding.searchView.isIconified = false
                binding.searchView.requestFocus()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT)
            } else {
                closeSearchView()
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterGlobalPokemon(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterGlobalPokemon(newText ?: "")
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            closeSearchView()
            false
        }

        binding.ivLogout.setOnClickListener {
            Firebase.auth.signOut()

            // XÓA TRẠNG THÁI ĐĂNG NHẬP KHI LOGOUT
            val sharedPreferences =
                getSharedPreferences("PokedexPrefs", android.content.Context.MODE_PRIVATE)
            sharedPreferences.edit { putBoolean("isLoggedIn", false) }

            Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // LOGIC TÌM KIẾM TỰ ĐỘNG KÉO HỆ TỪ LINK GỐC CHẠY NGẦM
    private fun filterGlobalPokemon(query: String) {
        if (query.isEmpty()) {
            adapter.filterList(listSumary)
            return
        }

        val filteredList = ArrayList<DataPokemon>()
        val searchString = query.lowercase().trim()

        for (item in allGlobalPokemonNames) {
            if (item.name.lowercase().contains(searchString)) {
                val urlParts = item.url.trimEnd('/').split("/")
                val id: Int = urlParts.lastOrNull()?.toIntOrNull() ?: 0

                if (id > 0) {
                    val officialImageUrl =
                        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

                    // 1. Kiểm tra xem con này đã được tải ở danh sách phân trang trước đó chưa
                    val cachedPokemon = listSumary.find { it.id == id }

                    if (cachedPokemon != null) {
                        // Nếu đã có hệ sẵn rồi -> Thêm vào list tìm kiếm hiển thị ngay
                        filteredList.add(
                            DataPokemon(
                                id = id,
                                name = item.name,
                                types = cachedPokemon.types,
                                imageUrl = officialImageUrl
                            )
                        )
                    } else {
                        // 2. Tạo một Object tạm với mảng hệ rỗng để không bị văng app
                        val pokemonFake = DataPokemon(
                            id = id,
                            name = item.name,
                            types = listOf(),
                            imageUrl = officialImageUrl
                        )
                        filteredList.add(pokemonFake)

                        // Tự gọi link gốc lấy đúng hệ về đắp vào list
                        val currentDetailUrl = item.url
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val url = URL(currentDetailUrl)
                                val conn = url.openConnection() as HttpURLConnection
                                conn.requestMethod = "GET"

                                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                                    val resText =
                                        conn.inputStream.bufferedReader().use { it.readText() }
                                    val jsonObj = JSONObject(resText)
                                    val typesArray = jsonObj.getJSONArray("types")

                                    val realTypes = ArrayList<String>()
                                    for (j in 0 until typesArray.length()) {
                                        val typeName = typesArray.getJSONObject(j)
                                            .getJSONObject("type").getString("name")
                                        realTypes.add(typeName)
                                    }

                                    // Lấy được hệ chuẩn gốc -> Cập nhật ngược lại vào bộ nhớ Cache và thông báo cho Adapter
                                    withContext(Dispatchers.Main) {
                                        val newPokemon =
                                            DataPokemon(id, item.name, realTypes, officialImageUrl)
                                        listSumary.add(newPokemon) // Thêm vào cache để lần sau gõ không cần tải lại

                                        // Cập nhật hệ trực tiếp vào phần tử đang hiển thị trên màn hình search
                                        val indexInFiltered =
                                            filteredList.indexOfFirst { it.id == id }
                                        if (indexInFiltered != -1) {
                                            filteredList[indexInFiltered] = newPokemon
                                            adapter.notifyItemChanged(indexInFiltered)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
        adapter.filterList(filteredList)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun obseverViewModel() {
        viewModel.pokeList.observe(this) { data ->
            val oldSize = listSumary.size
            val newItemsCount = data.size - oldSize
            if (newItemsCount > 0) {
                listSumary.clear()
                listSumary.addAll(data)

                if (binding.searchView.isGone) {
                    adapter.updateData(listSumary)
                }

                if (oldSize == 0) {
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.notifyItemRangeInserted(oldSize, newItemsCount)
                }
            }
        }
    }

    fun scrollDown() {

        binding.rvPokemonList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && bottomNavVisibility) {
                    bottomNavVisibility = false
                    binding.bottomNavigation.animate()
                        .translationY(binding.bottomNavigation.height.toFloat()).setDuration(80)
                        .withEndAction { binding.bottomNavigation.visibility = View.GONE }
                    if (binding.searchView.isVisible) return
                    binding.bottomNavigation.clearAnimation()
                } else if (dy < -0 && !bottomNavVisibility) {
                    binding.bottomNavigation.animate().translationY(0f).setDuration(80)
                        .withEndAction { binding.bottomNavigation.visibility = View.VISIBLE }
                    bottomNavVisibility = true
                    binding.bottomNavigation.clearAnimation()
                }
                val layoutManger = recyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManger.childCount
                val totalItemCount = layoutManger.itemCount
                val pasVisibleItems = layoutManger.findFirstVisibleItemPosition()
                if (!viewModel.isLoading) {
                    if ((visibleItemCount + pasVisibleItems) >= totalItemCount) {
                        viewModel.fetchPokemonList()
                    }
                }
            }
        })
    }

    fun setupBottomNavigation() {
        binding.bottomNavigation.itemIconTintList = null
        binding.bottomNavigation.selectedItemId = R.id.nav_pokedex

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_pokedex -> true
                R.id.nav_move_items -> {
                    startActivity(Intent(this, Item_MoveActivity::class.java))
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

    //Chuẩn bị nút bấm cho các item naviagtion
    fun setupNavigationDrawer() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_friends -> {
                    showSearchFriendBottomSheet()
                    binding.drawerLayout.closeDrawers()
                    return@setNavigationItemSelectedListener true // Trả về true để hệ thống biết mình đã xử lý xong
                }

                // Nếu có các nút khác (Ví dụ: Cài đặt, Đăng xuất...) thì viết tiếp ở đây
                // R.id.nav_settings -> { ... }

                else -> false
            }
        }
    }

    //Hiện thanh tìm kiếm + danh sách bạn bè + lời mời kết bạn
    fun showSearchFriendBottomSheet() {
        val bottomSheet = BottomSheetDialog(this)
        val bindingBottomSheet = SearchFriendLayoutBinding.inflate(layoutInflater)
        bottomSheet.setContentView(bindingBottomSheet.root)

        val sharedPreferences = getSharedPreferences("PokedexPrefs", Context.MODE_PRIVATE)
        val myUserName = sharedPreferences.getString("currentUsername", "") ?: " "

        //Gọi databaseRealTime
        val db =
            FirebaseDatabase.getInstance("https://pokedex-e01de-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users")

        val currentList = ArrayList<String>()
        //set Adapter
        val friendAdapter = FriendAdapter(currentList) { clicked ->
            // Bước 1: Trỏ vào hòm thư của chính mình xem thằng 'clicked' này có đang chờ duyệt không?
            val checkRequestRef = db.child(myUserName).child("FriendRequests").child(clicked)
            checkRequestRef.get().addOnSuccessListener {snapshot ->
                if(snapshot.exists() && snapshot.value.toString() == "pending"){
                    //Trường hợp đã gửi lời mời
                    Toast.makeText(this, "Đang xử lý kết bạn với $clicked", Toast.LENGTH_SHORT).show()

                    //Cập nhật 3 nơi
                    val updates = hashMapOf<String, Any?>(
                        "$myUserName/FriendRequests/$clicked" to null, //Xóa thư mời, gán bằng null
                        "$myUserName/Friends/$clicked" to true,        //Thêm vào list bạn bè
                        "$clicked/Friends/$myUserName" to true         //Thêm mình vào list bạn bè đối phương
                    )

                    db.updateChildren(updates).addOnSuccessListener {
                        Toast.makeText(this, "Bạn và $clicked đã trở thành bạn bè", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Lỗi mạng, ko thể kết bạn", Toast.LENGTH_SHORT).show()
                    }

                }else{
                    //lời mời từ người lạ
                    Toast.makeText(this, "Đang gửi lời mời kết bạn đến $clicked", Toast.LENGTH_SHORT).show()
                    //Gửi lời mời kết bạn
                    val requestRef = db.child(clicked)
                        .child("FriendRequests")
                        .child(myUserName)

                    requestRef.setValue("pending").addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Đã gửi lời mời thành công đến $clicked",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        bottomSheet.dismiss()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Lỗi mạng không thể gửi", Toast.LENGTH_SHORT).show()
                    }

                }
            }

        }
        //Gắn adapter vào recyclerView
        bindingBottomSheet.rvFriends.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        bindingBottomSheet.rvFriends.adapter = friendAdapter

        //Gọi hàm lắng nghe lời mời
        listenForFriendRequests(bindingBottomSheet,friendAdapter)

        //Xử lý tìm kiếm trên firebase
        bindingBottomSheet.btnSearchFriend.setOnClickListener {
            val keyword = bindingBottomSheet.edtSearchFriend.text.toString().trim()

            if (keyword.isEmpty()) {
                bindingBottomSheet.edtSearchFriend.error = "Hãy nhập tên bạn bè muốn tìm kiếm"
                bindingBottomSheet.edtSearchFriend.requestFocus()
                return@setOnClickListener
            }

            bindingBottomSheet.tvListTitle.text = "Đang rà soát máy chủ..."
            //truy van database
            db.orderByKey().startAt(keyword).endAt(keyword + "\uf8ff").get()
                .addOnSuccessListener { snapshot ->
                    val listFound = ArrayList<String>()
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val foundName = userSnapshot.key.toString()
                            if (foundName != myUserName) {
                                listFound.add(foundName)
                            }
                        }
                    }

                    if (listFound.isNotEmpty()) {
                        bindingBottomSheet.tvListTitle.text =
                            "Tìm thấy ${listFound.size} Trainer khớp với $keyword"
                        friendAdapter.updateData(listFound)
                    } else {
                        bindingBottomSheet.tvListTitle.text =
                            "Không thấy trainer nào có tên $keyword quanh đây"
                        friendAdapter.updateData(emptyList())
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Lỗi mạng, mất kết nối", Toast.LENGTH_SHORT).show()
                    bindingBottomSheet.tvListTitle.text = "Lỗi kết nối"
                }
        }

        bottomSheet.show()
    }

    //Hàm nhận lời mời kết bạn
    private fun listenForFriendRequests(
        bindingBottomSheet: SearchFriendLayoutBinding,
        currentAdapter: FriendAdapter
    ) {
        val sharedPreferences = getSharedPreferences("PokedexPrefs", Context.MODE_PRIVATE)
        val myUserName = sharedPreferences.getString("currentUsername", "") ?: " "

        if (myUserName.isEmpty()) return
        //Gọi databaseRealTime
        val db =
            FirebaseDatabase.getInstance("https://pokedex-e01de-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users")

        val myRequest = db.child(myUserName)

        //RealTime: lắng nghe sự kiện
        myRequest.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listRequest = ArrayList<String>()
                val listFriends = ArrayList<String>()

                if (snapshot.hasChild("FriendRequests")) {
                    for (req in snapshot.child("FriendRequests").children) {
                        if (req.value.toString() == "pending") {
                            listRequest.add(req.key.toString())
                        }
                    }
                }

                if (snapshot.hasChild("Friends")){
                    for (friends in snapshot.child("Friends").children){
                        listFriends.add(friends.key.toString())
                    }
                }
                currentAdapter.updateFriendsList(listFriends)
                //Cap nhat len giao dien
                if (listRequest.isNotEmpty()) {
                    bindingBottomSheet.tvListTitle.text = "Lời mời kết bạn (${listRequest.size})"
                    currentAdapter.updateData(listRequest)
                } else if(listFriends.isNotEmpty()){
                    bindingBottomSheet.tvListTitle.text = "Bạn bè của tôi (${listFriends.size})"
                    currentAdapter.updateData(listFriends)
                }else{
                    bindingBottomSheet.tvListTitle.text = "Bạn bè của tôi: 0"
                    currentAdapter.updateData(emptyList())
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@MainActivity, "Lỗi đọc hòm thư", Toast.LENGTH_SHORT).show()
            }

        })

    }
}