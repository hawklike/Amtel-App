package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.TeamDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TeamManager
{
    suspend fun addTeam(name: String, tmId: String, playingDays: List<String>, place: String): Team? = withContext(Dispatchers.IO)
    {
        val team = Team(name = name, tmId = tmId, playingDays = playingDays, place = place)
        return@withContext try
        {
            TeamDAO().insert(team)
            Log.i(TAG, "addUser(): $team successfully added to database")
            team
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "addUser(): $team not added to database because $ex")
            null
        }
    }

    private const val TAG = "TeamManager"
}