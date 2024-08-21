package com.example.stylish.assets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import java.util.Locale

class CapitalizeFirstTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(formatText(text), type)
    }

    private fun formatText(text: CharSequence?): CharSequence? {
        return if (!text.isNullOrEmpty()) {
            val firstChar = text[0].uppercase(Locale.getDefault())
            val restOfText = text.substring(1).lowercase(Locale.getDefault())
            firstChar + restOfText
        } else {
            text
        }
    }
}
