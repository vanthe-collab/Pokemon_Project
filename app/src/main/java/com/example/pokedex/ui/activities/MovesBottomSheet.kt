package com.example.pokedex.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.data.model.DataPokemonMove
import com.example.pokedex.data.model.DataPokemonMoveForSelection
import com.example.pokedex.data.model.moveList
import com.example.pokedex.databinding.LayoutMovesBottomSheetBinding
import com.example.pokedex.ui.adapters.MoveSelectionAdapter
import com.example.pokedex.viewmodels.DetailViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MovesBottomSheet(
    private val onMoveSelected: (DataPokemonMoveForSelection) -> Unit
) : BottomSheetDialogFragment() {
    private val viewModel: DetailViewModel by activityViewModels()
    private lateinit var binding: LayoutMovesBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = LayoutMovesBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = MoveSelectionAdapter { selectedMove ->
            onMoveSelected(selectedMove)
            dismiss() // Chọn xong tự động đóng bảng trượt xuống dưới đáy
        }
        viewModel.selectionPaginator.liveData.observe(viewLifecycleOwner) { moveList ->
            if (moveList.isNotEmpty()) {
                adapter.submitList(moveList!!)
            }

        }
        binding.rvBottomSheetMoves.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBottomSheetMoves.adapter = adapter


        //xu ly cuon xuong day
        binding.rvBottomSheetMoves.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val pastVisibleItem = layoutManager.findFirstVisibleItemPosition()

                    if (!viewModel.selectionPaginator.isLoading && !viewModel.selectionPaginator.isLoadComplete) {
                        if ((visibleItemCount + pastVisibleItem) >= totalItemCount){
                            viewModel.selectionPaginator.onScrollToBottom(viewModel.viewModelScope)
                        }
                    }
                }
            }
        })
    }


}