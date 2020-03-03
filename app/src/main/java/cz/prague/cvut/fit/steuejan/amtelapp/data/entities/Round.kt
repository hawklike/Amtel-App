package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

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
                 var homePlayersId: MutableList<String> = mutableListOf(),
                 var awayPlayersId: MutableList<String> = mutableListOf(),
                 var homePlayers: MutableList<Player> = mutableListOf(),
                 var awayPlayers: MutableList<Player> = mutableListOf()
                 ) : Parcelable
