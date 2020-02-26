package cz.prague.cvut.fit.steuejan.amtelapp.fragments.match

import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMatchActivityFragment

object MatchViewFactory
{
    fun getFragment(title: String): AbstractMatchActivityFragment
    {
        return when(title)
        {
            "Zápis utkání" -> MatchInputResultFragment.newInstance()
            "Výsledek utkání" -> MatchResultFragment.newInstance()
            else -> throw IllegalArgumentException("Fragment with the title $title doesn't exist.")
        }
    }
}