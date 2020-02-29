package cz.prague.cvut.fit.steuejan.amtelapp.data.util

import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import java.util.*

enum class Day
{
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    override fun toString(): String
    {
        return when(this)
        {
            MONDAY -> context.getString(R.string.monday)
            TUESDAY -> context.getString(R.string.tuesday)
            WEDNESDAY -> context.getString(R.string.wednesday)
            THURSDAY -> context.getString(R.string.thursday)
            FRIDAY -> context.getString(R.string.friday)
            SATURDAY -> context.getString(R.string.saturday)
            SUNDAY -> context.getString(R.string.sunday)
        }
    }
}

fun String.toDayInWeek(): Day
{
    return when(this.trim().toLowerCase(Locale.getDefault()))
    {
        context.getString(R.string.monday) -> Day.MONDAY
        context.getString(R.string.tuesday) -> Day.TUESDAY
        context.getString(R.string.wednesday) -> Day.WEDNESDAY
        context.getString(R.string.thursday) -> Day.THURSDAY
        context.getString(R.string.friday) -> Day.FRIDAY
        context.getString(R.string.saturday) -> Day.SATURDAY
        context.getString(R.string.sunday) -> Day.SUNDAY
        else -> throw IllegalArgumentException("given day in a week doesn't exist")
    }
}