package com.example.pokedex.ui.fragments

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.example.pokedex.R
import com.example.pokedex.databinding.FragmentBaseStatsBinding
import androidx.core.graphics.toColorInt
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.pokedex.viewmodels.DetailViewModel

class FragmentBaseStats : Fragment(R.layout.fragment_base_stats) {
    private var _binding: FragmentBaseStatsBinding? = null
    private val binding get() = _binding!!
    val viewModel: DetailViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBaseStatsBinding.bind(view)
        obseverViewModel()
    }

    fun obseverViewModel() {
        viewModel.fragmentStats.observe(viewLifecycleOwner) { data ->
            val mapList = data.stats.associate { it.stat.name to it.base_stat }
            val hpValue = mapList["hp"] ?: 0
            val attackValue = mapList["attack"] ?: 0
            val defValue = mapList["defense"] ?: 0
            val spValue = mapList["speed"] ?: 0
            val spaValue = mapList["special-attack"] ?: 0
            val spdValue = mapList["special-defense"] ?: 0
            val total = mapList.values.sum()
            binding.tvStatHP.text = hpValue.toString()
            binding.tvStatAT.text = attackValue.toString()
            binding.tvStatDef.text = defValue.toString()
            binding.tvStatSPAtk.text = spaValue.toString()
            binding.tvStatSPDef.text = spdValue.toString()
            binding.tvStatSpeed.text = spValue.toString()
            binding.tvStatTotal.text = total.toString()

            setEffectProgressBar(binding.pbStatHP, hpValue)
            setEffectProgressBar(binding.pbStatSpeed, spValue)
            setEffectProgressBar(binding.pbStatSPDef, spdValue)
            setEffectProgressBar(binding.pbStatSPAtk, spaValue)
            setEffectProgressBar(binding.pbStatDef, defValue)
            setEffectProgressBar(binding.pbStatAT, attackValue)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    fun setEffectProgressBar(progressBar: ProgressBar, value: Int) {
        val colorHex = when {
            value <= 100 -> "#FFA726"
            value in 101..150 -> "#4CAF50"
            else -> "#00897B"
        }

        val colorInt = colorHex.toColorInt()
        progressBar.progressTintList = ColorStateList.valueOf(colorInt)
        ObjectAnimator.ofInt(
            progressBar,
            "progress",
            0,
            value
        ).setDuration(1000).start()
    }
}