package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Group(override var id: String? = null,
                 var name: String = "",
                 var teamIds: MutableMap<String, MutableList<String>> = mutableMapOf(), //(year, teamIds)
                 var rounds: MutableMap<String, Int> = mutableMapOf(), //(year, #rounds)
                 var roundDates: MutableMap<String, Int> = mutableMapOf(), //(round, weekInYear)
                 var rank: Int = Int.MAX_VALUE - 1,
                 var playingPlayOff: Boolean = true,
                 var playOff: Boolean = false,
                 var playOffStart: Date = Date()
                 ) : Parcelable, Entity<Group>()
{
    fun deepCopy(id: String? = this.id,
                 name: String = this.name,
                 teamIds: MutableMap<String, MutableList<String>> = this.teamIds.toMutableMap(),
                 rounds: MutableMap<String, Int> = this.rounds.toMutableMap(),
                 roundDates: MutableMap<String, Int> = this.rounds.toMutableMap(),
                 rank: Int = this.rank,
                 playingPlayOff: Boolean = this.playingPlayOff,
                 playOff: Boolean = this.playOff,
                 playOffStart: Date = this.playOffStart)
            = Group(id, name, teamIds, rounds, roundDates, rank, playingPlayOff, playOff, playOffStart)
}