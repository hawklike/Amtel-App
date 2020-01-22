package cz.prague.cvut.fit.steuejan.amtelapp.data.util

enum class UserRole
{
    HEAD_OF_LEAGUE, TEAM_MANAGER;

    companion object
    {
        fun isTM(role: UserRole): Boolean
        {
            return when(role)
            {
                HEAD_OF_LEAGUE -> false
                TEAM_MANAGER -> true
            }
        }
    }
}