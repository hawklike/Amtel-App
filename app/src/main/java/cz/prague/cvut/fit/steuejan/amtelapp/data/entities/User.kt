package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(var id: String = "",
                var name: String = "",
                var surname: String = "",
                var email: String = "",
                var isTM: Boolean = true) : Parcelable
