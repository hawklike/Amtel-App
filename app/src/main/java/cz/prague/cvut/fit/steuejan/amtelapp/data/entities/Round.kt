package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Round(var homeSets: Int? = null,
                 var awaySets: Int? = null,
                 var homeGems: Int? = null,
                 var awayGems: Int? = null,
                 var homeGemsSet1: Int? = null,
                 var awayGemsSet1: Int? = null,
                 var homeGemsSet2: Int? = null,
                 var awayGemsSet2: Int? = null,
                 var homeGemsSet3: Int? = null,
                 var awayGemsSet3: Int? = null,
                 var homePlayers: MutableList<Player> = mutableListOf(),
                 var awayPlayers: MutableList<Player> = mutableListOf(),
                 var homeWinner: Boolean? = null,
                 var matchId: String = "",
                 var date: Date = Date(),
                 var round: Int = 1
                 ) : Parcelable
