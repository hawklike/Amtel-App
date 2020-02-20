package cz.prague.cvut.fit.steuejan.amtelapp.data.util

enum class UserOrderBy
{
    NAME, SURNAME, TEAM, EMAIL, SEX;

    override fun toString(): String
    {
        return when(this)
        {
            NAME -> "name"
            SURNAME -> "surname"
            TEAM -> "teamName"
            EMAIL -> "email"
            SEX -> "sex"
        }
    }
}