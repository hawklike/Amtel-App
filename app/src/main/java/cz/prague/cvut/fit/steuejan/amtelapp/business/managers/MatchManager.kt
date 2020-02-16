package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.MatchDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match
import cz.prague.cvut.fit.steuejan.amtelapp.states.MatchState
import cz.prague.cvut.fit.steuejan.amtelapp.states.NoMatch
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidMatch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object MatchManager
{
    private const val TAG = "MatchManager"

    suspend fun addMatch(match: Match): MatchState = withContext(IO)
    {
        return@withContext try
        {
            MatchDAO().insert(match)
            Log.i(TAG, "addMatch(): $match successfully added to database")
            ValidMatch(match)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "addMatch(): $match not added to database because $ex")
            NoMatch
        }
    }
}