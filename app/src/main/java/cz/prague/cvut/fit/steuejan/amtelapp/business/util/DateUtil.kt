package cz.prague.cvut.fit.steuejan.amtelapp.business.util

import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Day
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

    fun getWeekDate(week: Int): List<Date>
    {
        val cal = Calendar.getInstance()
        cal[Calendar.WEEK_OF_YEAR] = week
        cal[Calendar.DAY_OF_WEEK] = Calendar.MONDAY

        return mutableListOf<Date>(cal.time).apply {
            (1..6).map {
                cal.add(Calendar.DATE, 1)
                this.add(cal.time)
            }
        }
    }

    fun findDate(homeDays: List<Day>, awayDays: List<Day>, range: List<Date>): Date?
    {
        val days = homeDays.intersect(awayDays)

        if(days.isEmpty())
        {
            return try { range[homeDays.first().ordinal] }
            catch(ex: NoSuchElementException) { null }
        }

        return range[days.first().ordinal]
    }
}

fun Date.toMyString(format: String = "dd.MM.yyyy"): String
{
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.format(this)
}

fun Date.setTime(hours: Int, minutes: Int): Date
{
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.HOUR_OF_DAY, hours)
    cal.set(Calendar.MINUTE, minutes)
    return cal.time
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

fun String.toCalendar(dateFormat: String = "dd.MM.yyyy"): Calendar
{
    return Calendar.getInstance().apply {
        time = this@toCalendar.toDate(dateFormat)
    }
}