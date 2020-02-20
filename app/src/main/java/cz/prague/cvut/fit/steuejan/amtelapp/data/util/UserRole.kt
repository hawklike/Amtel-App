package cz.prague.cvut.fit.steuejan.amtelapp.data.util

enum class UserRole
{
    HEAD_OF_LEAGUE, TEAM_MANAGER, PLAYER;

    override fun toString(): String
    {
        return when(this)
        {
            HEAD_OF_LEAGUE -> "head_of_league"
            TEAM_MANAGER -> "team_manager"
            PLAYER -> "player"
        }
    }
}

fun String.toRole(): UserRole
{
    return when(this)
    {
        "head_of_league" -> UserRole.HEAD_OF_LEAGUE
        "team_manager" -> UserRole.TEAM_MANAGER
        "player" -> UserRole.PLAYER
        else -> throw IllegalArgumentException("given role doesn't exist")
    }
}