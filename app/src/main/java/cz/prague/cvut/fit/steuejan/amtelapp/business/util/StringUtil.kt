package cz.prague.cvut.fit.steuejan.amtelapp.business.util

import java.util.*

object StringUtil
{
    //TODO: uncomment this
    fun getRandomString(length: Int) : String
    {
//        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz1234567890"
//        return (1..length)
//            .map { allowedChars.random() }
//            .joinToString("")

        return "123456"
    }

    fun createLabel(text: String): CharSequence
    {
        return text.fold(StringBuilder()) { acc, c ->
            if(acc.length > 2) return@fold acc
            acc.append(c)
        }
    }

    fun prepareCzechOrdering(name: String, surname: String): Pair<String, String>
    {
        val convertedName = changeToEnglishForm(name)
        val convertedSurname = changeToEnglishForm(surname)

        return Pair(convertedName, convertedSurname)
    }

    private fun changeToEnglishForm(text: String): String
    {
        return text.toLowerCase(Locale.getDefault())
            .replace("č", "czz")
            .replace("ch", "hzz")
            .replace("ď", "dzz")
            .replace("ř", "rzz")
            .replace("š", "szz")
            .replace("ť", "tzz")
            .replace("ž", "zzz")
    }
}

fun String.firstLetterUpperCase(): String
{
    val output = this.toLowerCase()
    return output.replaceRange(0, 1,
        this[0]
            .toUpperCase()
            .toString())
}

fun String.shrinkWhitespaces(): String =
    this.replace("\\s+".toRegex(), " ")

fun String.removeWhitespaces(): String =
    this.replace("\\s".toRegex(), "")