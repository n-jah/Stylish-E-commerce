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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
        setupSearch()
    }


//get the name of the user
// Define a callback for asynchronous data
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

        binding.apply {

            val sharedUserName = UserUtils.getUserNameFromSharedPreferences(requireContext())

            if (!sharedUserName.isNullOrBlank()) {
                greetingTextNameOfUser.visibility = View.VISIBLE
                greetingTextNameOfUser.text = sharedUserName
            }else{
                val nameInAuth = UserUtils.getDisplayName()
                val nameList = mutableListOf<String>()

                if (!nameInAuth.isNullOrBlank()) {
                    nameList.add(UserUtils.getFirstName(nameInAuth))
                    nameList.add(UserUtils.getLastName(nameInAuth))
                    greetingTextNameOfUser.visibility = View.VISIBLE
                    greetingTextNameOfUser.text = nameList[0]
                } else {
                    // Observe user info if nameInAuth is null or blank
                    viewModel.getUserInfo()
                    viewModel.userLiveData.observe(viewLifecycleOwner) { userInfo ->
                        userInfo?.let {
                            nameList.add(UserUtils.getFirstName(it.username))
                            nameList.add(UserUtils.getLastName(it.username))
                        }

                        // Update greeting text based on the nameList contents
                        if (nameList.isNotEmpty()) {
                            greetingTextNameOfUser.visibility = View.VISIBLE
                            greetingTextNameOfUser.text = nameList[0] // Use index 0 for first name
                        } else {
                            greetingTextNameOfUser.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
    private fun setupSearch() {

        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Hide other UI components when EditText gains focus
                toggleHomeUIVisibility(false)
            }else{
                toggleHomeUIVisibility(true)
            }
        }

        binding.searchEditText.setOnEditorActionListener { _, _, _ ->
            // When the user finishes searching and presses "Done"
            binding.searchEditText.clearFocus()
            toggleHomeUIVisibility(true)
            true
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                filterItems(query)
            }

            override fun afterTextChanged(s: Editable?) {}
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
            item.title.lowercase().contains(query) || item.brand.lowercase().contains(query)
        }

        filteredList?.let {
            itemAdapter.updateItems(it) // Create or use a method in your adapter to update the data
        }
    }
    override fun onResume() {
        super.onResume()
        binding.searchEditText.clearFocus() // Ensure the UI is reset when returning
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
            binding.searchEditText.setText(spokenText) // Set the recognized text to the EditText
        }
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


