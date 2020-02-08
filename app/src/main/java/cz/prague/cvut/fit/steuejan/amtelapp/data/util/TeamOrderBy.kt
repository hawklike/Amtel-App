package cz.prague.cvut.fit.steuejan.amtelapp.data.util

enum class TeamOrderBy
{
    NAME, GROUP;

    override fun toString(): String
    {
        return when(this)
        {
            NAME -> "name"
            GROUP -> "group"
        }
    }
}