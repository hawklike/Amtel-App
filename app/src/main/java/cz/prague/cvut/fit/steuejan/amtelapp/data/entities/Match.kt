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
                 var rounds: List<Round> = listOf(Round(), Round(), Round()),
                 var year: Int = -1,
                 var place: String? = null,
                 var dateAndTime: Date? = null
) : Parcelable, Entity()
{
    init
    {
        year = DateUtil.actualYear
    }
}