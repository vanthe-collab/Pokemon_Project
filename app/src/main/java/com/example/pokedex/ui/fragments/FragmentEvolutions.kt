package com.example.pokedex.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pokedex.R
import com.example.pokedex.ui.adapters.PokemonEvolutionChainAdapter
import com.example.pokedex.databinding.FragmentEvolutionBinding
import com.example.pokedex.viewmodels.DetailViewModel

class FragmentEvolutions : Fragment(R.layout.fragment_evolution) {
    private var _binding: FragmentEvolutionBinding? = null
    private val viewModel: DetailViewModel by activityViewModels()
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEvolutionBinding.bind(view)

        binding.rvEvolution.layoutManager = LinearLayoutManager(context,
            LinearLayoutManager.HORIZONTAL, false
        )
        obseverViewModel()
    }

    fun obseverViewModel() {
        viewModel.pokeEvolutionItem.observe(viewLifecycleOwner) { data ->
            val evoList = data
            val adapter = PokemonEvolutionChainAdapter(evoList){clickedId ->
                viewModel.xuLyId(clickedId)
            }
            binding.rvEvolution.adapter = adapter
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}