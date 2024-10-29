package com.example.stylish.ui.home.fragment

import FavoriteAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stylish.ViewModel.MainViewModel
import com.example.stylish.databinding.FragmentWishlistBinding
import com.example.stylish.repository.AuthRepositoryImpl
import com.example.stylish.repository.FirebaseBrandRepositry
import com.example.stylish.repository.FirebaseItemRepository
import com.example.stylish.repository.MainViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var favoriteAdapter: FavoriteAdapter
    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        Log.w("LogTag","onCreateView")

        return binding.root }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()

        // Setup RecyclerView
        setupRecyclerView()
    }
    private fun initViewModel() {
        auth = FirebaseAuth.getInstance()
        val mainFactory = MainViewModelFactory(FirebaseItemRepository(), FirebaseBrandRepositry(),
            AuthRepositoryImpl()
        )
        viewModel = ViewModelProvider(requireActivity(), mainFactory).get(MainViewModel::class.java)
        viewModel.userId = auth.currentUser?.uid ?: ""
        binding.progressBar.visibility = View.VISIBLE

        // Fetch favorite items
        viewModel.loadFavoriteItems()
    }
    private fun setupRecyclerView() {
        // Initialize FavoriteAdapter
        favoriteAdapter = FavoriteAdapter(viewModel)
        // Setup RecyclerView
        binding.favoriteRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.favoriteRecyclerView.adapter = favoriteAdapter
    }
    private fun initObservers() {
        // Observe favorite items only
        viewModel.favoriteItems.observe(viewLifecycleOwner, Observer { favoriteItems ->
            Log.d("WishlistFragment", "Favorite items observed: ${favoriteItems.size}")

            if (favoriteItems.isNullOrEmpty()) {
                favoriteAdapter.submitList(emptyList()) // Submit an empty list
                binding.favoriteRecyclerView.adapter = favoriteAdapter
                binding.favoriteRecyclerView.visibility = View.GONE
                binding.emptyWhishlistAnimation.visibility =View.VISIBLE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.favoriteRecyclerView.visibility = View.VISIBLE
                binding.emptyWhishlistAnimation.visibility = View.GONE
                favoriteAdapter.submitList(favoriteItems) // Efficient update with DiffUtil
                binding.progressBar.visibility = View.GONE
            }

        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding to prevent memory leaks
        viewModel.favoriteItems.removeObservers(viewLifecycleOwner)
    }

    override fun onResume() {
        super.onResume()
     initObservers()
    }
}
