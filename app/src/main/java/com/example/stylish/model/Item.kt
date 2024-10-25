package com.example.stylish.model

import android.os.Parcel
import android.os.Parcelable

data class Item(
    val title: String = "",
    val price: Double = 0.0,
    val imgUrl: ArrayList<String> = ArrayList(),
    val size: ArrayList<String> = ArrayList(),
    val description: String = "",
    val id: String = "",
    val itemsInStock: Int = 0,
    val rating: Double = 0.0,
    val brand: String = "",
    var isFavorite: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.createStringArrayList() ?: ArrayList(),
        parcel.createStringArrayList() ?: ArrayList(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeDouble(price)
        parcel.writeStringList(imgUrl)
        parcel.writeStringList(size)
        parcel.writeString(description)
        parcel.writeString(id)
        parcel.writeInt(itemsInStock)
        parcel.writeDouble(rating)
        parcel.writeString(brand)
        parcel.writeByte(if (isFavorite) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item {
            return Item(parcel)
        }

        override fun newArray(size: Int): Array<Item?> {
            return arrayOfNulls(size)
        }
    }
}
