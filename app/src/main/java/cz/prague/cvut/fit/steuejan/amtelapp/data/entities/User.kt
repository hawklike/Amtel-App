package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class User(var id: String = "",
                var name: String = "",
                var surname: String = "",
                var email: String = "",
                var phone: String? = null,
                var birthdate: Date? = null,
                var sex: Boolean = true,
                var role: String = "",
                var teamId: String = "",
                var matchesId: MutableList<String> = mutableListOf(),
                var firstSign: Boolean = true
                ) : Parcelable
