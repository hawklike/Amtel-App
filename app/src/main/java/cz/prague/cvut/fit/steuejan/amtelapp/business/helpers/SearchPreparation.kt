package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers

import org.apache.commons.lang3.StringUtils
import java.util.Locale.getDefault

@Suppress("MemberVisibilityCanBePrivate", "unused")
class SearchPreparation(text: String = "")
{
    var preparedText = prepareText(text)
        private set

    val acronyms = ACRONYMS.map { it.toLowerCase(getDefault()) }.toMutableSet()

    var longestAcronym = acronyms.maxBy { it.length }?.length
        private set

    fun addAcronyms(vararg acronyms: String): Boolean
    {
        this.acronyms.addAll(acronyms).run {
            longestAcronym = acronyms.maxBy { it.length }?.length
            return this
        }
    }

    //removes diacritics and converts the text to trimmed lower case text
    fun prepareText(text: String): String
            =  StringUtils.stripAccents(text).toLowerCase(getDefault()).trim()

    fun doCompleteSearch(textToSearch: String): Boolean
            = doCompleteSearch2(textToSearch)

    //checks if a complete search is needed or not
    fun doCompleteSearch(): Boolean
            = doCompleteSearch2(preparedText)

    //uses text given as an argument in constructor
    fun removeSportClubAcronym(vararg acronyms: String): String
    {
        acronyms.forEach { this.acronyms.add(it.toLowerCase(getDefault())) }
        longestAcronym = acronyms.maxBy { it.length }?.length
        preparedText = removeSportClubAcronym(preparedText)
        return preparedText
    }

    //uses name given as an argument to this function, the name is name of a sports club
    fun removeSportClubAcronym(name: String, acronyms: Array<out String> = arrayOf()): String
    {
        acronyms.forEach { this.acronyms.add(it.toLowerCase(getDefault())) }
        longestAcronym = acronyms.maxBy { it.length }?.length
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

    //if the text is on 100 % without acronym, the function returns false (complete search is not needed)
    //complete search means a search with a possible acronym
    private fun doCompleteSearch2(text: String): Boolean
    {
        if(text.length <= longestAcronym ?: 0) return true
        acronyms.forEach {
            if(text.startsWith("$it ", true)) return true
        }
        return false
    }

    companion object
    {
        val ACRONYMS = listOf("TK", "TJ", "TC", "SK", "SC")
    }
}