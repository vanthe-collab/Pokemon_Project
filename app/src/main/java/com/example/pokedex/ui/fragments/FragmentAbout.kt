package com.example.pokedex.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.pokedex.R
import com.example.pokedex.databinding.FragmentAboutBinding
import com.example.pokedex.viewmodels.DetailViewModel

class FragmentAbout : Fragment(R.layout.fragment_about) {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by activityViewModels()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAboutBinding.bind(view)
        observeViewModel()
    }

    fun observeViewModel() {
        viewModel.fragmentStats.observe(viewLifecycleOwner) { data ->
            binding.tvWeight.text = "${data.weight / 10.0} kg"
            binding.tvHeight.text = "${data.height / 10.0} m"
            binding.tvAbility.text = data.abilities.joinToString(", ") { it.ability.name }
        }
        viewModel.pokemonSpeciesResponse.observe(viewLifecycleOwner) { data ->
            binding.tvFriendShip.text = data.base_happiness.toString()
            binding.tvSpecies.text = data.genera.find { it.language.name == "en" }?.genus ?: " "
            val rawDes =
                data.flavor_text_entries.find { it.language.name == "en" }?.flavor_text ?: " "
            binding.tvDescription.text = rawDes.replace("\n", " ").replace("\u000c", " ")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}