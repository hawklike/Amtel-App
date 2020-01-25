package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Team(var id: String = "",
                var name: String = "",
                var tmId: String = "",
                var playingDays: List<String> = emptyList(),
                var place: String = "",
                var usersId: List<String> = emptyList(),
                var matchesId: List<String> = emptyList()
                ) : Parcelable