package cz.prague.cvut.fit.steuejan.amtelapp.data.util

enum class UserRole
{
    HEAD_OF_LEAGUE, TEAM_MANAGER, NONE;

    companion object
    {
        fun toString(role: UserRole): String
        {
            return when(role)
            {
                HEAD_OF_LEAGUE -> "head_of_league"
                TEAM_MANAGER -> "team_manager"
                NONE -> "none"
            }
        }

        fun toRole(text: String): UserRole
        {
            return when(text)
            {
                "head_of_league" -> HEAD_OF_LEAGUE
                "team_manager" -> TEAM_MANAGER
                "none" -> NONE
                else -> throw IllegalArgumentException("given role doesn't exist")
            }
        }
    }
}