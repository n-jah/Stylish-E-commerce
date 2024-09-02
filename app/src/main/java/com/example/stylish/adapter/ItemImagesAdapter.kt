    package com.example.stylish.adapter

    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Adapter
    import android.widget.ImageView
    import androidx.core.content.ContextCompat
    import androidx.recyclerview.widget.RecyclerView
    import com.bumptech.glide.Glide
    import com.bumptech.glide.load.resource.bitmap.CenterInside
    import com.bumptech.glide.request.RequestOptions
    import com.example.stylish.R
    class ItemImagesAdapter(private val images: ArrayList<String>, private val onImageSelected: (String) -> Unit) : RecyclerView.Adapter<ItemImagesAdapter.ImageViewHolder>() {
        var selectedPosition = RecyclerView.NO_POSITION

        inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.imageItemId)

            init {
                itemView.setOnClickListener {
                    val previousPosition = selectedPosition
                    selectedPosition = adapterPosition

                    // Notify changes for re-rendering
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)

                    // Notify the Activity of the selected image
                    onImageSelected(images[selectedPosition])
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sample_image_item, parent, false)
            return ImageViewHolder(view)
        }

        override fun getItemCount(): Int = images.size

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val currentImage = images[position]
            val requestOptions = RequestOptions().transforms(CenterInside())

            // Load image into the imageView
            Glide.with(holder.itemView.context)
                .load(currentImage)
                .apply(requestOptions)
                .into(holder.imageView)

            // Update UI for selected item
            if (position == selectedPosition) {
                holder.itemView.setBackgroundResource(R.drawable.selected_default_item_background)
            } else {
                holder.itemView.setBackgroundResource(R.drawable.default_item_background)
            }
        }
    }
