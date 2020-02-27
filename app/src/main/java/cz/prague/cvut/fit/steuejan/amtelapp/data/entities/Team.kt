package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Team(override var id: String? = null,
                var name: String = "",
                var tmId: String = "",
                var playingDays: List<String> = emptyList(),
                var place: String = "",
                var usersId: MutableList<String> = mutableListOf(),
                var users: MutableList<User> = mutableListOf(),
                var matches: MutableList<Match> = mutableListOf(),
                var group: String? = null
                ) : Parcelable, Entity()