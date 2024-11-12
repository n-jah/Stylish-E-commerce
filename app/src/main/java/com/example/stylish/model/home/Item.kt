package com.example.stylish.model.home

import android.os.Parcel
import android.os.Parcelable

data class Item(
    val title: String = "",
    val price: Double = 0.0,
    val imgUrl: ArrayList<String> = ArrayList(),
    val sizes: List<Size> = listOf(), // Changed to List<Size> to store size and stock objects
    val description: String = "",
    val id: String = "",
    val rating: Double = 0.0,
    val brand: String = "",
    var isFavorite: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        title = parcel.readString() ?: "",
        price = parcel.readDouble(),
        imgUrl = parcel.createStringArrayList() ?: ArrayList(),
        sizes = parcel.createTypedArrayList(Size) ?: listOf(),
        description = parcel.readString() ?: "",
        id = parcel.readString() ?: "",
        rating = parcel.readDouble(),
        brand = parcel.readString() ?: "",
        isFavorite = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeDouble(price)
        parcel.writeStringList(imgUrl)
        parcel.writeTypedList(sizes) // Write the list of Size objects to the Parcel
        parcel.writeString(description)
        parcel.writeString(id)
        parcel.writeDouble(rating)
        parcel.writeString(brand)
        parcel.writeByte(if (isFavorite) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item {
            return Item(parcel)
        }

        override fun newArray(size: Int): Array<Item?> {
            return arrayOfNulls(size)
        }
    }
}
