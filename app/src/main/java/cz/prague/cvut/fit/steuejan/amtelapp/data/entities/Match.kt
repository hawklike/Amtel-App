package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

import android.os.Parcelable
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Match(override var id: String? = null,
                 var group: String = "",
                 var round: Int = 0,
                 var home: String = "",
                 var away: String = "",
                 var homeId: String = "",
                 var awayId: String = "",
                 var homeScore: Int? = null,
                 var awayScore: Int? = null,
                 var rounds: MutableList<Round> = mutableListOf(Round(), Round(), Round()),
                 var year: Int = DateUtil.actualYear.toInt(),
                 var place: String? = null,
                 var dateAndTime: Date? = null,
                 var edits: MutableMap<String, Int> = mutableMapOf("1" to 2, "2" to 2, "3" to 2), //(round, free edits)
                 val usersId: MutableList<String?> = arrayOfNulls<String>(10).toMutableList(),
                 var defaultEndGameEdits: Int = 2
                 ) : Parcelable, Comparable<Match>, Entity()
{
    override fun compareTo(other: Match): Int =
        compareBy<Match> { it.id }.thenBy { it.home }.compare(this, other)
}
