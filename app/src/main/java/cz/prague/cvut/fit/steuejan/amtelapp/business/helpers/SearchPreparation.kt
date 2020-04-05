package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers

import org.apache.commons.lang3.StringUtils
import java.util.Locale.getDefault

@Suppress("MemberVisibilityCanBePrivate", "unused")
class SearchPreparation(text: String = "")
{
    var preparedText = prepareText(text)
        private set

    val acronyms = ACRONYMS.map { it.toLowerCase(getDefault()) }.toMutableSet()

    fun prepareText(text: String): String
            =  StringUtils.stripAccents(text).toLowerCase(getDefault()).trim()

    fun removeSportClubAcronym(vararg acronyms: String): String
    {
        acronyms.forEach { this.acronyms.add(it.toLowerCase(getDefault())) }
        preparedText = removeSportClubAcronym(preparedText)
        return preparedText
    }

    fun removeSportClubAcronym(name: String, acronyms: Array<out String> = arrayOf()): String
    {
        acronyms.forEach { this.acronyms.add(it.toLowerCase(getDefault())) }
        return removeSportClubAcronym(prepareText(name))
    }

    private fun removeSportClubAcronym(name: String): String
    {
        acronyms.forEach {
            with("$it ") {
                if(name.startsWith(this))
                    return name.replaceFirst(this, "")
            }
        }
        return name
    }

    companion object
    {
        val ACRONYMS = listOf("TK", "TJ", "TC", "SK", "SC")
    }
}