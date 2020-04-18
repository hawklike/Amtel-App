package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Player(var playerId: String = "",
                  var name: String = "",
                  var surname: String = "",
                  var email: String = "",
                  var birthdate: Date? = null,
                  var sex: Boolean = true,
                  var isHome: Boolean? = null
                  ) : Parcelable, Comparable<Player>
{
    override fun toString(): String
    {
        return "$name $surname – $email"
    }

    override fun compareTo(other: Player): Int =
        compareBy<Player>{ it.playerId }.compare(this, other)
}