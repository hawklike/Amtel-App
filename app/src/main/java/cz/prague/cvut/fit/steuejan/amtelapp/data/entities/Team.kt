package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Team(override var id: String? = null,
                var name: String = "",
                var tmId: String = "",
                var playingDays: List<String> = emptyList(),
                var place: String? = null,
                var usersId: MutableList<String> = mutableListOf(),
                var users: MutableList<User> = mutableListOf(),
                var matchesId: MutableList<String> = mutableListOf(),
                var pointsPerMatch: MutableMap<String, MutableMap<String, Int>> = mutableMapOf(), //(year, (id_match, points)),
                var pointsPerYear:  MutableMap<String, Int> = mutableMapOf(), //(year, points),
                var group: String? = null
                ) : Parcelable, Entity()