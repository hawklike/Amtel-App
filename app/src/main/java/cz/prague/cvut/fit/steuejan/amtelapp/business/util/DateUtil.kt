package cz.prague.cvut.fit.steuejan.amtelapp.business.util

import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Day
import org.joda.time.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtil
{
    fun validateDate(date: String, dateFormat: String = DATE_FORMAT): Boolean
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
    fun validateBirthdate(date: String, dateFormat: String = DATE_FORMAT): Boolean
    {
        if(!validateDate(date, dateFormat)) throw Exception("Invalid date")
        return DateTime(Date()) >= DateTime(date.toDate(dateFormat))
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

    fun getWeekNumber(date: Date): Int
    {
        val cal = Calendar.getInstance()
        cal[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        cal.time = date
        return cal[Calendar.WEEK_OF_YEAR]
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

    fun getRemainingDaysUntil(week: Int): Int
    {
        val lastDay = DateTime(getWeekDate(week).last().time).withHourOfDay(23).withMinuteOfHour(59)
        val today = DateTime()
        val remainingDays = Days.daysBetween(today, lastDay).days
        return if(remainingDays < 0) 0 else remainingDays
    }

    fun getRemainingDaysUntil(date: Date): Int
    {
        val converted = DateTime(date).withHourOfDay(23).withMinuteOfHour(59)
        val today = DateTime()
        val remainingDays = Days.daysBetween(today, converted).days
        return if(remainingDays < 0) 0 else remainingDays
    }

    fun compareDates(first: Date?, second: Date?): Int
            = DateTimeComparator.getDateOnlyInstance().compare(DateTime(first), DateTime(second))

    fun compareDatesWithTime(first: Date?, second: Date?): Int
            = DateTimeComparator.getInstance().compare(DateTime(first), DateTime(second))

    fun getAge(birthdate: Date): Int
            = Years.yearsBetween(LocalDate(birthdate), LocalDate()).years

    fun getDateInFuture(days: Int, startDate: Date? = null): Date
    {
        val cal = Calendar.getInstance()
        cal.time = startDate?.let { it } ?: Date()
        cal.add(Calendar.DATE, days)
        return cal.time
    }

    fun isDateBetween(date: Date?, startDate: Date?, endDate: Date?): Boolean
    {
        return if(compareDates(startDate, date) <= 0) compareDates(date, endDate) <= 0
        else false
    }

    const val DATE_FORMAT = "dd.MM.yyyy"

    internal var actualSeason: String = "0"

    private val actualYear: Int
        get()
        {
            val calendar = GregorianCalendar()
            calendar.time = Date()
            return calendar[Calendar.YEAR]
        }

    internal var serverTime: Date? = null
}

fun Date.toMyString(format: String = DateUtil.DATE_FORMAT): String
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

fun Date.toCalendar(): Calendar
{
    val cal = Calendar.getInstance()
    cal.time = this
    return cal
}

fun Calendar.toMyString(format: String = DateUtil.DATE_FORMAT): String
{
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.format(this.time)
}

fun String.toDate(dateFormat: String = DateUtil.DATE_FORMAT): Date
{
    return SimpleDateFormat(dateFormat, Locale.getDefault()).parse(this)
}

fun String.toCalendar(dateFormat: String = DateUtil.DATE_FORMAT): Calendar
{
    return Calendar.getInstance().apply {
        time = this@toCalendar.toDate(dateFormat)
    }
}