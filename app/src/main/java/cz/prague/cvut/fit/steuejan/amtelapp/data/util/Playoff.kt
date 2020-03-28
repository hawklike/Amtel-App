package cz.prague.cvut.fit.steuejan.amtelapp.data.util

import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import java.util.*

class Playoff(val name: String, val startDate: Date)
{
    val endDate = DateUtil.getDateInFuture(PLAYOFF_DAYS - 1, startDate)

    val isActive: Boolean
        get() = DateUtil.isDateBetween(null, startDate, endDate)

    companion object
    {
        const val PLAYOFF_DAYS = 14
    }
}

fun Group.toPlayoff(): Playoff?
{
    return if(this.playOff) Playoff(
        this.name,
        this.playOffStart
    )
    else null
}

