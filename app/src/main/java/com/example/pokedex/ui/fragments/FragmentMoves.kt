package com.example.pokedex.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.databinding.FragmentMovesBinding
import com.example.pokedex.ui.adapters.PokemonMoveAdapter
import com.example.pokedex.viewmodels.DetailViewModel

class FragmentMoves: Fragment(R.layout.fragment_moves) {
    private val viewModel: DetailViewModel by activityViewModels()
    private lateinit var moveAdapter: PokemonMoveAdapter
    private var _binding : FragmentMovesBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovesBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moveAdapter = PokemonMoveAdapter()
        binding.rvMoves.layoutManager = LinearLayoutManager(requireContext())

        binding.rvMoves.adapter = moveAdapter
        viewModel.moveTabPaginator.liveData.observe(viewLifecycleOwner){moveList ->
            if (moveList.isNotEmpty()){
                moveAdapter.submitList(moveList)
            }
        }

        binding.rvMoves.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // dy > 0 nghĩa là đang vuốt xuống
                if (dy > 0) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                    // Kiểm tra xem đã chạm đáy chưa
                    if (!viewModel.moveTabPaginator.isLoadComplete && !viewModel.moveTabPaginator.isLoading) {
                        if (totalItemCount > 0 && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            viewModel.moveTabPaginator.onScrollToBottom(viewModel.viewModelScope)
                        }
                    }
                }
            }
        })
    }




    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}