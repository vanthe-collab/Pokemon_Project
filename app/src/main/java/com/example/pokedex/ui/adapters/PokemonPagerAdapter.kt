package com.example.pokedex.ui.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pokedex.ui.fragments.FragmentAbout
import com.example.pokedex.ui.fragments.FragmentBaseStats
import com.example.pokedex.ui.fragments.FragmentEvolutions
import com.example.pokedex.ui.fragments.FragmentMoves

class PokemonPagerAdapter(fragmentActivity: FragmentActivity, private val pokemonId: Int) :
    FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val fragment = FragmentAbout()
                return fragment
            }

            1 -> {
                val fragment = FragmentBaseStats()
                return fragment
            }

            2 -> {
                val fragment = FragmentEvolutions()
                return fragment
            }
            3 -> {
                val fragment = FragmentMoves()
                return fragment
            }

            else -> FragmentAbout()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }


}