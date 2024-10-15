package com.example.stylish.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stylish.ViewModel.MainViewModel
import com.example.stylish.adapter.BrandAdapter
import com.example.stylish.adapter.ItemAdapter
import com.example.stylish.databinding.FragmentHomeBinding
import com.example.stylish.repository.FirebaseItemRepository
import com.example.stylish.repository.FirebaseBrandRepositry
import com.example.stylish.repository.MainViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize ViewModel
        initViewModel()

        // Setup RecyclerViews
        setupRecyclerViews()

        // Observe data
        initObservers()

        return binding.root
    }

    private fun setupRecyclerViews() {
        // Setup Brands RecyclerView
        binding.brandsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.brandsRecyclerView.adapter = BrandAdapter(isLoading = true)

        // Setup Items RecyclerView
        binding.newArrivalRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.newArrivalRecyclerView.adapter = ItemAdapter(isLoading = true)
    }

    private fun initViewModel() {
        val mainFactory = MainViewModelFactory(FirebaseItemRepository(), FirebaseBrandRepositry())
        viewModel = ViewModelProvider(this, mainFactory).get(MainViewModel::class.java)
    }

    private fun initObservers() {
        // Observe brands data
        viewModel.brands.observe(viewLifecycleOwner, Observer { brands ->
            if (brands != null) {
                binding.brandsRecyclerView.adapter = BrandAdapter(brands, isLoading = false)
            }
        })

        // Observe items data
        viewModel.items.observe(viewLifecycleOwner, Observer { items ->
            if (items != null) {
                binding.newArrivalRecyclerView.adapter = ItemAdapter(items, isLoading = false)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding reference
    }
}
