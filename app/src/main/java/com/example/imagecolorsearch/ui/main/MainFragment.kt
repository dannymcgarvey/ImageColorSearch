package com.example.imagecolorsearch.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.imagecolorsearch.R
import com.example.imagecolorsearch.databinding.DialogSearchBinding
import com.example.imagecolorsearch.databinding.FragmentMainBinding
import com.example.imagecolorsearch.recycler.BitmapAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: BitmapAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = BitmapAdapter()
        binding.thumbnailRecycler.adapter = adapter
        lifecycleScope.launch {
            viewModel.filteredThumbnails.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val showCancelButton = viewModel.searchParams.value.filter.isNotEmpty()
        menu.findItem(R.id.app_bar_cancel_search).apply {
            isEnabled = showCancelButton
            isVisible = showCancelButton
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_bar_cancel_search -> {
                viewModel.searchParams.update {
                    SearchParams(
                        emptyList(),
                        it.filterThreshold,
                        it.minimumDensity,
                        it.requireAll
                    )
                }
                return true
            }
            R.id.app_bar_search -> {
                val dialogSearchBinding =
                    DialogSearchBinding.inflate(LayoutInflater.from(requireContext()))

                val currentSearchParams = viewModel.searchParams.value

                val colorAdapter = ColorAdapter(requireContext()).apply {
                    selectedColors.addAll(currentSearchParams.filter)
                }

                dialogSearchBinding.colorGrid.adapter = colorAdapter

                dialogSearchBinding.sliderThreshold.value =
                    currentSearchParams.filterThreshold.toFloat()

                dialogSearchBinding.sliderDensity.value =
                    currentSearchParams.minimumDensity.toFloat()

                dialogSearchBinding.sliderThreshold.setLabelFormatter(PercentageFormatter)
                dialogSearchBinding.sliderDensity.setLabelFormatter(PercentageFormatter)

                dialogSearchBinding.requireAll.isChecked =
                    currentSearchParams.requireAll

                MaterialAlertDialogBuilder(requireContext())
                    .setView(dialogSearchBinding.root)
                    .setPositiveButton("Search") {_, _ ->
                        viewModel.searchParams.update {
                            SearchParams(
                                colorAdapter.selectedColors.toList(),
                                dialogSearchBinding.sliderThreshold.value.toDouble(),
                                dialogSearchBinding.sliderDensity.value.toDouble(),
                                dialogSearchBinding.requireAll.isChecked
                            )
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .show()
                return true
            }
        }
        return false
    }

}