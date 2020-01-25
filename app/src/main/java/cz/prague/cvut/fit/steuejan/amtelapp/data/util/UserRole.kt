package cz.prague.cvut.fit.steuejan.amtelapp.data.util

enum class UserRole
{
    HEAD_OF_LEAGUE, TEAM_MANAGER, PLAYER;

    companion object
    {
        fun toString(role: UserRole): String
        {
            return when(role)
            {
                HEAD_OF_LEAGUE -> "head_of_league"
                TEAM_MANAGER -> "team_manager"
                PLAYER -> "player"
            }
        }

        fun toRole(text: String): UserRole
        {
            return when(text)
            {
                "head_of_league" -> HEAD_OF_LEAGUE
                "team_manager" -> TEAM_MANAGER
                "player" -> PLAYER
                else -> throw IllegalArgumentException("given role doesn't exist")
            }
        }
    }
}