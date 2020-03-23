package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Group(var name: String = "",
                 var teamIds: MutableMap<String, MutableList<String>> = mutableMapOf(), //(year, teamIds)
                 var rounds: MutableMap<String, Int> = mutableMapOf(), //(year, #rounds)
                 var roundDates: MutableMap<String, Int> = mutableMapOf(), //(round, weekInYear)
                 var rank: Int = Int.MAX_VALUE - 1,
                 var playingPlayOff: Boolean = true,
                 var playOff: Boolean = false
                 ) : Parcelable, Entity()