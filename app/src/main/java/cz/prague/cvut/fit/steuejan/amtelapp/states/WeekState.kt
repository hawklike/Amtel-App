package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import java.util.*

sealed class WeekState
{
    companion object
    {
        fun validate(weekText: String): WeekState
        {
            if(weekText.isEmpty()) return InvalidWeek("Vyplňte prosím týden v roce.")

            val week: Int
            try { week = weekText.toInt() }
            catch(ex: NumberFormatException) { return InvalidWeek("Vyplňte prosím týden v roce jako číslo.") }

            if(week < 1 || week > 53) return InvalidWeek("Týden v roce může nabývat hodnot 1–53.")

            return ValidWeek(week, DateUtil.getWeekDate(week))
        }
    }
}

data class ValidWeek(val self: Int, val range: Pair<Date, Date>) : WeekState()
data class InvalidWeek(val errorMessage: String) : WeekState()