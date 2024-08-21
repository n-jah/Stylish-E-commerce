package com.example.stylish.ui.auth.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.stylish.databinding.FragmentSartBinding

class SartFragment : Fragment() {

    private var fragmentChangeListener: FragmentChangeListener? = null
    private lateinit var binding: FragmentSartBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangeListener)
        {
            fragmentChangeListener = context
        } else
        {
            throw RuntimeException("$context must implement FragmentChangeListener")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSartBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val signInText = binding.signInText
        signInText.setOnClickListener {
            fragmentChangeListener?.replaceFragment(LoginFragment())
        }
    }
    override fun onDetach() {
        super.onDetach()
        fragmentChangeListener = null
    }
}

interface FragmentChangeListener {
    fun replaceFragment(fragment: Fragment)
}