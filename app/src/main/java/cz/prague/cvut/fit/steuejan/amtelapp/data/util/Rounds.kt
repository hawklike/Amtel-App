package cz.prague.cvut.fit.steuejan.amtelapp.data.util

import android.os.Parcelable
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Rounds(var first: Round? = null, var second: Round? = null, var third: Round? = null) : Parcelable
{
    fun getRound(roundPosition: Int): Round?
    {
        return when(roundPosition)
        {
            1 -> first
            2 -> second
            3 -> third
            else -> throw IllegalArgumentException("roundPosition of this number doesn't exist, use roundPosition from 1 to 3")
        }
    }

    fun getActiveRounds(): List<Round>
    {
        val rounds = mutableListOf<Round>()
        (1..3).map {
            getRound(it)?.run { rounds.add(this) }
        }
        return rounds
    }

    fun setRound(round: Round?, roundPosition: Int): Rounds
    {
        when(roundPosition)
        {
            1 -> first = round
            2 -> second = round
            3 -> third = round
            else -> throw IllegalArgumentException("rounds have only position from 1 to 3, use roundPosition from 1 to 3")
        }
        return this
    }
}