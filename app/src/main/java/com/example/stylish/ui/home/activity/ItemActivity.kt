package com.example.stylish.ui.home.activity

import android.content.Intent
import android.graphics.Color
import android.media.Rating
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestOptions
import com.example.stylish.R
import com.example.stylish.adapter.ItemImagesAdapter
import com.example.stylish.adapter.SizeAdapter
import com.example.stylish.databinding.ActivityItemBinding
import com.example.stylish.model.Item
import com.example.stylish.model.Size

class ItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemBinding
    private lateinit var backButton : ImageButton



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT

        binding = ActivityItemBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_item)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getBundle()

        binding.cartButton.setOnClickListener{
            startActivity(Intent(this, CartActivity::class.java))
        }



    }
    private fun imgsinti( imges: ArrayList<String>) {
        val rv = binding.itemImagesRv
        rv.layoutManager = LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL, false)
        rv.adapter = ItemImagesAdapter(imges){selectedImg ->
            Glide.with(this)
                .load(selectedImg)
                .apply(RequestOptions().transforms(CenterInside()))
                .into(findViewById(R.id.itemImage))
        }


    }
    private fun sizeInti(sizes : List<Size>) {
        val rv = binding.itemSizesRv
        rv.layoutManager = LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL, false)
        rv.adapter = SizeAdapter(sizes)



    }

    private fun getBundle() {
        val item = intent.getParcelableExtra<Item>("object")
        backButton = binding.backButton
        backButton.setOnClickListener {
            finish()
        }

        if (item != null) {
            binding.productTitle.text = item.title
            binding.productPrice.text = item.price.toString()
            binding.totalPrice.text = (item.price *1.15 ).toFloat().toString()
            binding.ratingNumber.text = item.rating.toString()
            binding.ratingStars.rating  =item.rating.toFloat()
            binding.description.text = item.description


            val requestOptions = RequestOptions().transforms(CenterInside())
            Glide.with(this)
                .load(item.imgUrl[0])
                .apply(requestOptions)
                .into(binding.itemImage)


            val  sizes:MutableList<Size> = mutableListOf()
            val sizeObjectList = item.size
            for( n in sizeObjectList){
                sizes.add(Size(n))
            }
            sizeInti(sizes)
            imgsinti(item.imgUrl)

        } else {
            // Handle the case where item is null
            binding.productTitle.text = "Item not found"
            // You might also want to log an error or show a message to the user
        }
    }

}