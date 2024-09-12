package com.example.stylish.ui.auth.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.example.stylish.databinding.FragmentVerificationBinding


class VerificationFragment : Fragment() {
    private var _binding: FragmentVerificationBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTextWatchers()
    }

    private fun setupTextWatchers(){
        binding.etDigit1.addTextChangedListener(GenericTextWatcher(binding.etDigit1, binding.etDigit2))
        binding.etDigit2.addTextChangedListener(GenericTextWatcher(binding.etDigit2, binding.etDigit3))
        binding.etDigit3.addTextChangedListener(GenericTextWatcher(binding.etDigit3, binding.etDigit4))
        binding.etDigit4.addTextChangedListener(GenericTextWatcher(binding.etDigit4, null)) // No next focus for the last one
    }

    inner class GenericTextWatcher(private val currentView: View, private val nextView: View?) : TextWatcher{
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.length == 1) {
                nextView?.requestFocus()
            }
            // If this is the last EditText, hide the keyboard
            if (currentView == binding.etDigit4) {
                hideKeyboard(currentView)
            }
        }
    }

    // Helper function to hide the keyboard
    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
