import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestOptions
import com.example.stylish.R
import com.example.stylish.ViewModel.MainViewModel
import com.example.stylish.adapter.ItemDiffCallback
import com.example.stylish.assets.UpdateFavoriteCallback
import com.example.stylish.model.Item
import com.example.stylish.ui.home.activity.ItemActivity
import com.google.firebase.auth.FirebaseAuth

class FavoriteAdapter(
    private val viewModel: MainViewModel
) : ListAdapter<Item, FavoriteAdapter.ItemViewHolder>(ItemDiffCallback()) {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.product_img_card)
        val itemName: TextView = itemView.findViewById(R.id.itemNameCard)
        val itemPrice: TextView = itemView.findViewById(R.id.itemPrice_incard)
        val favicon: ImageView = itemView.findViewById(R.id.favicon)

        fun bind(itemModel: Item, context: Context) {
            itemName.text = itemModel.title
            itemPrice.text = itemModel.price.toString()
            val requestOptions = RequestOptions().transforms(CenterInside())

            // Load image with error handling
            Glide.with(context)
                .load(itemModel.imgUrl[0].toString())
                .apply(requestOptions)

                .into(itemImage)

            // Set favorite icon based on the item's favorite state
            favicon.setImageResource(
                if (itemModel.isFavorite) R.drawable.favorites else R.drawable.hear_checkd
            )

            favicon.setOnClickListener {
                val newFavoriteState = !itemModel.isFavorite
                // Update the item's state and notify the change
                itemModel.isFavorite = newFavoriteState

                viewModel.updateFavoriteState(itemModel.id, newFavoriteState, object :
                    UpdateFavoriteCallback {
                    override fun onSuccess() {
                        if (newFavoriteState) {
                            removeItem(bindingAdapterPosition)

                            viewModel.loadFavoriteItems() // Load items after updating
                            notifyItemRemoved(bindingAdapterPosition) // Use bindingAdapterPosition

                        } else {
                            notifyItemChanged(bindingAdapterPosition) // Use bindingAdapterPosition
                            viewModel.loadFavoriteItems() // Load items after updating

                            notifyItemChanged(bindingAdapterPosition) // Use bindingAdapterPosition



                        }
                        Log.d("FavoriteAdapter", "Favorite state updated successfully")
                    }

                    override fun onFailure() {

                        Log.e("ItemAdapter", "Failed to update favorite state")


                    }
                })
            }

            itemView.setOnClickListener {
                val intent = Intent(context, ItemActivity::class.java)
                intent.putExtra("object", itemModel)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_card_cloth, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position), holder.itemView.context)

    }

    // Method to remove an item from the list
    private fun removeItem(position: Int) {
        val currentList = currentList.toMutableList() // Get a mutable copy of the current list
        viewModel.updateFavoriteState(getItem(position).id, false,object : UpdateFavoriteCallback {

            override fun onSuccess() {
                // Handle success if needed
                currentList.removeAt(position) // Remove the item at the specified position
                submitList(currentList) // Submit the updated list to the adapter
                Log.d("FavoriteAdapter", "Item removed at position $position")
            }
            override fun onFailure() {
                // Handle failure if needed
                Log.e("FavoriteAdapter", "Failed to remove item at position $position")
            }
        }) // Update the item's state in the ViewModel



    }
}
