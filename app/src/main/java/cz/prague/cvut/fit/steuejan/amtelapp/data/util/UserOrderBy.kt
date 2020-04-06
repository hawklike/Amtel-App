package cz.prague.cvut.fit.steuejan.amtelapp.data.util

enum class UserOrderBy
{
    NAME, SURNAME, TEAM, EMAIL, SEX, ROLE, AGE;

    override fun toString(): String
    {
        return when(this)
        {
            NAME -> "englishName"
            SURNAME -> "englishSurname"
            TEAM -> "teamName"
            EMAIL -> "email"
            SEX -> "sex"
            ROLE -> "role"
            AGE -> "birthdate"
        }
    }
}