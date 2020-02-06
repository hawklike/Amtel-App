package cz.prague.cvut.fit.steuejan.amtelapp.business.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtil
{
    fun toString(date: Date, format: String = "dd.MM.yyyy"): String
    {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }

    fun toString(date: Calendar, format: String = "dd.MM.yyyy"): String
    {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date.time)
    }

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
        return stringToDate(dateToString(Date(), dateFormat), dateFormat) >= stringToDate(date, dateFormat)
    }

    fun stringToDate(date: String, dateFormat: String = "dd.MM.yyyy"): Date
    {
        return SimpleDateFormat(dateFormat, Locale.getDefault()).parse(date)
    }

    private fun dateToString(date: Date, dateFormat: String): String
    {
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        return sdf.format(date)
    }
}