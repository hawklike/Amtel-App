package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Rounds
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlayerRounds(override var id: String? = "playerRounds",
                        var rounds: MutableMap<String, Rounds> = mutableMapOf() //(matchId, rounds)
) : Parcelable, Entity<PlayerRounds>()