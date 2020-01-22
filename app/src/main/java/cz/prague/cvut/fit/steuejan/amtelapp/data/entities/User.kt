package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(val id: String,
                val name: String,
                val surname: String,
                val email: String,
                val isTM: Boolean) : Parcelable
