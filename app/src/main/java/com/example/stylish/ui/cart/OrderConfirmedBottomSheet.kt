package com.example.stylish.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.stylish.databinding.BottomSheetOrderConfirmedBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OrderConfirmedBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetOrderConfirmedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetOrderConfirmedBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var onDismissListener: (() -> Unit)? = null
    override fun dismiss() {
        super.dismiss()
        onDismissListener?.invoke()
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.goToOrdersButton.setOnClickListener {
            startActivity(Intent(requireContext(), OrdersActivity::class.java))
            dismiss()


        }
        binding.continueShoppingButton.setOnClickListener {
            onDestroyView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDismissListener?.invoke()
        _binding = null
    }

    companion object {
        fun newInstance(): OrderConfirmedBottomSheet {
            return OrderConfirmedBottomSheet()
        }
    }
}
