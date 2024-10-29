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
import com.example.stylish.repository.AuthRepositoryImpl
import com.example.stylish.repository.FirebaseItemRepository
import com.example.stylish.repository.FirebaseBrandRepositry
import com.example.stylish.repository.MainViewModelFactory
import com.example.stylish.utilities.UserUtils
import com.google.firebase.auth.FirebaseAuth


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
    }
    private fun initUI() {
        addingNameToUi(binding)
    }


//get the name of the user
// Define a callback for asynchronous data
    private fun setupRecyclerViews() {
        // Setup Brands RecyclerView
        brandAdapter = BrandAdapter(isLoading = true)
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
                brandAdapter = BrandAdapter(brands, isLoading = false)
                binding.brandsRecyclerView.adapter = brandAdapter
             }else{
                binding.brandsRecyclerView.adapter = BrandAdapter(isLoading = true)
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding reference
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

}


