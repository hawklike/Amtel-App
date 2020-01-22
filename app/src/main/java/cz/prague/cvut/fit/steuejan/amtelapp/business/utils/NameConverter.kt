package cz.prague.cvut.fit.steuejan.amtelapp.business.utils

import java.util.*

object NameConverter
{
    fun convertToFirstLetterBig(textToBeConverted: String): String
    {
        val output = textToBeConverted.toUpperCase(Locale.getDefault())
        return output.replaceRange(0, 1,
            textToBeConverted[0].toString().toUpperCase(Locale.getDefault()))
    }
}