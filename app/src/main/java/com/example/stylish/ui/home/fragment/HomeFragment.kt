package com.example.stylish.ui.home.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stylish.ViewModel.home.MainViewModel
import com.example.stylish.adapter.BrandAdapter
import com.example.stylish.adapter.ItemAdapter
import com.example.stylish.databinding.FragmentHomeBinding
import com.example.stylish.repository.auth.AuthRepositoryImpl
import com.example.stylish.repository.home.FirebaseItemRepository
import com.example.stylish.repository.home.FirebaseBrandRepositry
import com.example.stylish.ViewModel.home.MainViewModelFactory
import com.example.stylish.model.home.Brand
import com.example.stylish.utilities.UserUtils
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var brandAdapter: BrandAdapter
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize ViewModel
        initViewModel()
        // Setup RecyclerViews
        setupRecyclerViews()
        // Observe data
        initObservers()
        //seatingUp the UI
        initUI()
        // Set up the voice recognition button to start voice recognition
        setupVoiceSearch()
    }
    private fun initUI() {
        addingNameToUi(binding)
        setupSearchBar()
        binding.swipfreshlayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun refreshData() {

        lifecycleScope.launch {

            viewModel.apply {
                fetchItemsWithFavorites()
                loadFavoriteItems()
                getUserData()
            }
            itemAdapter.isLoading = true
            delay(1000)
            brandAdapter.updateBrands(viewModel.brands.value?.toList() ?: emptyList() )
            itemAdapter.isLoading = false
            binding.swipfreshlayout.isRefreshing = false
        }
    }


    //get the name of the user
    private fun setupRecyclerViews() {
        // Setup Brands RecyclerView
        brandAdapter = BrandAdapter(isLoading = true){ _ ->
        }
        binding.brandsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.brandsRecyclerView.adapter = brandAdapter // Keep only this line

        // Setup Items RecyclerView
        itemAdapter = ItemAdapter(isLoading = true, viewModel = viewModel)
        binding.newArrivalRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.newArrivalRecyclerView.adapter = itemAdapter // Keep only this line
    }
    private fun initViewModel() {
        auth = FirebaseAuth.getInstance()
        val mainFactory = MainViewModelFactory(FirebaseItemRepository(), FirebaseBrandRepositry(), AuthRepositoryImpl())
        viewModel = ViewModelProvider(requireActivity(), mainFactory).get(MainViewModel::class.java)
        viewModel.userId = auth.currentUser?.uid ?: ""
        viewModel.fetchItemsWithFavorites()
    }
    private fun initObservers() {
        // Observe brands data
        viewModel.brands.observe(viewLifecycleOwner, Observer { brands ->
            if (brands != null) {
                brandAdapter.updateBrands(brands)
                brandAdapter = BrandAdapter(brands, isLoading = false){selectedBrand ->
                    filterItemsByBrand(selectedBrand)
                }
                binding.brandsRecyclerView.adapter = brandAdapter
             }else{
                binding.brandsRecyclerView.adapter = BrandAdapter(isLoading = true){_->

                }
            }
        })
        // Observe items data
        viewModel.items.observe(viewLifecycleOwner, Observer { items ->
            if (items.isEmpty()) {
                viewModel.fetchItemsWithFavorites()
            }
            else {
                itemAdapter.updateItems(items) // Create a method in ItemAdapter to update data
                itemAdapter.isLoading = false
            }

        })
    }

    private fun filterItemsByBrand(selectedBrand: Brand) {
        if (selectedBrand.brandName != "All") {
            val filteredList = viewModel.items.value?.filter { item ->
                item.brand == selectedBrand.brandName.lowercase()
            }
            Log.d("HomeFragment", "Filtered items: $filteredList")
            Log.d("HomeFragment", "Selected brand: ${selectedBrand.brandName}")

            filteredList?.let {
             itemAdapter.updateItems(it) // Update the items in the adapter based on the selected brand

            }
        }else{
            val items = viewModel.items.value
            itemAdapter.updateItems(items?: emptyList())
        }
    }

    private fun addingNameToUi(binding: FragmentHomeBinding) {
        viewModel.userDataLiveData.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                binding.greetingTextNameOfUser.text = it.username
            }
        })
    }
    private fun setupSearchBar() {
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterItems(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterItems(it) }
                return true
            }
        })
    }
    private fun toggleHomeUIVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.chooseBrandText.visibility = visibility
        binding.newArrivalText.visibility = visibility
        binding.brandsRecyclerView.visibility = visibility
        binding.greetingTextNameOfUser.visibility = visibility
        binding.welcomeText.visibility = visibility
    }

    private fun filterItems(query: String) {
        val filteredList = viewModel.items.value?.filter { item ->
            item.title.lowercase().contains(query, ignoreCase = true) || item.brand.lowercase().contains(query)
        }

        filteredList?.let {
            itemAdapter.updateItems(it) // Create or use a method in your adapter to update the data
        }
    }
    override fun onResume() {
        super.onResume()
        toggleHomeUIVisibility(true)
    }

    private fun setupVoiceSearch() {
        // Set the listener for the voice search icon/button
        binding.voiceSearchIcon.setOnClickListener {
            checkAudioPermission()
        }
    }

    private val voiceRecognitionResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.get(0) ?: ""

            addVoiceSearchText(spokenText)
        }
    }
    private fun addVoiceSearchText(spokenText: String) {
        binding.searchBar.apply {
            setQuery(spokenText, false) // Set the query without submitting
            isIconified = false         // Expand the SearchView
            requestFocus()              // Request focus for the SearchView
        }
        filterItems(spokenText) // Trigger filtering explicitly
    }
    private fun checkAudioPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceRecognition()
            }
            else -> {
                requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startVoiceRecognition() {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        voiceRecognitionResultLauncher.launch(recognizerIntent)
    }

    // Declare the activity result launcher for requesting audio permissions
    private val requestAudioPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, proceed with voice recognition
            startVoiceRecognition()
        } else {
            // Permission denied, show a message
            Toast.makeText(requireContext(), "Microphone permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding reference
    }





}


