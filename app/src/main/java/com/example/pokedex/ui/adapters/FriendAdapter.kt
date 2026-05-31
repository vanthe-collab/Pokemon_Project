package com.example.pokedex.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.databinding.ItemFriendBinding

class FriendAdapter(private var listUser: List<String>, private val onAddFriend: (String) -> Unit,): RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {
    private var myFriendsList = listOf<String>()
    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(p0.context),p0,false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(
        p0: FriendViewHolder,
        p1: Int
    ) {
        val username = listUser[p1]
        p0.binding.btnAddFriend.isEnabled = true
        // 1. Đắp tên vào TextView
        p0.binding.tvFriendName.text = username
        //Ẩn hiện nút nếu đã là bạn bè
        if (myFriendsList.contains(username)) {
            p0.binding.btnAddFriend.visibility = View.GONE
        } else {
            p0.binding.btnAddFriend.visibility = View.VISIBLE
        }

        p0.binding.btnAddFriend.setOnClickListener {
            //Nhấn 1 lần ăn luôn ko cho nhấn nữa
            p0.binding.btnAddFriend.isEnabled = false
            onAddFriend(username) // Trả cái tên
        }
    }

    override fun getItemCount(): Int {
        return listUser.size
    }

    inner class FriendViewHolder(val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root)
    fun updateData(list: List<String>){
        listUser = list
        notifyDataSetChanged()
    }
    fun updateFriendsList(list: List<String>){
        myFriendsList = list
        notifyDataSetChanged()
    }
}