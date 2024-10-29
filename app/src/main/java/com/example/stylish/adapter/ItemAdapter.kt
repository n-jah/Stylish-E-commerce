    package com.example.stylish.adapter

    import android.annotation.SuppressLint
    import android.content.Context
    import android.content.Intent
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ImageView
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView
    import com.bumptech.glide.Glide
    import com.bumptech.glide.load.resource.bitmap.CenterInside
    import com.bumptech.glide.request.RequestOptions
    import com.example.stylish.R
    import com.example.stylish.ViewModel.MainViewModel
    import com.example.stylish.utilities.UpdateFavoriteCallback
    import com.example.stylish.model.Item

    import com.example.stylish.ui.home.activity.ItemActivity
    import com.facebook.shimmer.ShimmerFrameLayout

    @Suppress("DEPRECATION")
    class ItemAdapter(
          var itemList: List<Item> = emptyList(),
        var isLoading: Boolean = true ,  // Default value to avoid nulls
      var viewModel: MainViewModel // Pass the ViewModel

    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val VIEW_TYPE_ITEM = 0
        private val VIEW_TYPE_SHIMMER = 1
        private val shimmerItemCount = 10 // Configurable number of shimmer placeholders

        inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemImage: ImageView = itemView.findViewById(R.id.product_img_card)
            val itemName: TextView = itemView.findViewById(R.id.itemNameCard)
            val itemPrice: TextView = itemView.findViewById(R.id.itemPrice_incard)
            val favicon: ImageView = itemView.findViewById(R.id.favicon)
            @SuppressLint("SuspiciousIndentation")
            fun bind(itemModel: Item, context: Context) {
                itemName.text = itemModel.title
                itemPrice.text = itemModel.price.toString()
                val requestOptions = RequestOptions().transforms(CenterInside())
                    Glide.with(context)
                        .load(itemModel.imgUrl[0].toString())
                        .apply(requestOptions)
                        .into(itemImage)

                if (itemModel.isFavorite) {
                    favicon.setImageResource(R.drawable.hear_checkd)
                }else{
                    favicon.setImageResource(R.drawable.favorites)
                }
                favicon.setOnClickListener {
                    val newFavoriteState = !itemModel.isFavorite
                    itemModel.isFavorite = newFavoriteState


                    viewModel.updateFavoriteState(itemModel.id, newFavoriteState, object :
                        UpdateFavoriteCallback {

                        override fun onSuccess() {
                            // Handle success if needed
                            if (newFavoriteState) {

                                favicon.setImageResource(R.drawable.hear_checkd)
                                viewModel.loadFavoriteItems() // Load items after updating


                            } else {

                                favicon.setImageResource(R.drawable.favorites)
                                viewModel.loadFavoriteItems() // Load items after updating

                            }
                        }
                        override fun onFailure() {

                            Log.e("ItemAdapter", "Failed to update favorite state")
                        }
                    })

                }
            }
        }

        inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val shimmerFrameLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_item_cloth)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == VIEW_TYPE_ITEM) {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.sample_card_cloth, parent, false)
                ItemViewHolder(itemView)
            } else {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.sample_shimmer_card_cloth, parent, false)
                ShimmerViewHolder(itemView)
            }
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (getItemViewType(position) == VIEW_TYPE_ITEM) {

                val itemViewHolder = holder as ItemViewHolder
                val currentItem = itemList[position]
                itemViewHolder.bind(currentItem, holder.itemView.context)

                itemViewHolder.itemView.setOnClickListener {
                    // Handle item click here
                    val intent = Intent(holder.itemView.context, ItemActivity::class.java)

                    intent.putExtra("object",itemList[position])
                    holder.itemView.context.startActivity(intent)
                }

            } else {
                val shimmerViewHolder = holder as ShimmerViewHolder
                shimmerViewHolder.shimmerFrameLayout.startShimmer()
            }
        }

        override fun getItemCount(): Int {
            return if (isLoading) {
                shimmerItemCount // Number of shimmer placeholders
            } else {
                itemList.size
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (isLoading) VIEW_TYPE_SHIMMER else VIEW_TYPE_ITEM
        }


        fun updateItems(newItems: List<Item>) {
            itemList= newItems
            notifyDataSetChanged()

        }




    }

