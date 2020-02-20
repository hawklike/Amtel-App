package cz.prague.cvut.fit.steuejan.amtelapp.business.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtil
{
    fun validateDate(date: String, dateFormat: String = "dd.MM.yyyy"): Boolean
    {
        return try
        {
            val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(date)
            true
        }
        catch (e: ParseException) { false }
    }

    @Throws(Exception::class)
    fun validateBirthdate(date: String, dateFormat: String = "dd.MM.yyyy"): Boolean
    {
        if(!validateDate(date, dateFormat)) throw Exception("Invalid date")
        return Date().toMyString(dateFormat).toDate(dateFormat) >= date.toDate(dateFormat)
    }

    val actualYear: Int
        get()
        {
            val calendar = GregorianCalendar()
            calendar.time = Date()
            return calendar[Calendar.YEAR]
        }

    fun getWeekDate(week: Int): Pair<Date, Date>
    {
        val cal = Calendar.getInstance()
        cal[Calendar.WEEK_OF_YEAR] = week
        cal[Calendar.DAY_OF_WEEK] = Calendar.MONDAY

        val first = cal.time
        cal.add(Calendar.DATE, 6)
        val last = cal.time
        return Pair(first, last)
    }
}

fun Date.toMyString(format: String = "dd.MM.yyyy"): String
{
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.format(this)
}

fun Calendar.toMyString(format: String = "dd.MM.yyyy"): String
{
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.format(this.time)
}

fun String.toDate(dateFormat: String = "dd.MM.yyyy"): Date
{
    return SimpleDateFormat(dateFormat, Locale.getDefault()).parse(this)
}