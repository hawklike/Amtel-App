package cz.prague.cvut.fit.steuejan.amtelapp.states

import android.os.Parcelable
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import kotlinx.android.parcel.Parcelize
import java.util.*

sealed class WeekState
{
    companion object
    {
        fun validate(weekText: String): WeekState
        {
            if(weekText.isEmpty()) return InvalidWeek(context.getString(R.string.week_failure_empty_text))

            val week: Int
            try { week = weekText.toInt() }
            catch(ex: NumberFormatException) { return InvalidWeek(context.getString(R.string.week_failure_not_number_text)) }

            if(week < 1 || week > 53) return InvalidWeek(context.getString(R.string.week_failure_invalid_text))

            return ValidWeek(week, DateUtil.getWeekDate(week))
        }
    }
}

@Parcelize
data class ValidWeek(val self: Int, val range: List<Date>) : WeekState(), Parcelable
@Parcelize
data class InvalidWeek(val errorMessage: String = "") : WeekState(), Parcelable