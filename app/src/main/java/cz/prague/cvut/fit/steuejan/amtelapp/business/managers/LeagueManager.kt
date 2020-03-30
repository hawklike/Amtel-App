package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import com.google.firebase.firestore.ktx.toObject
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.LeagueDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.League
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object LeagueManager
{
    suspend fun getActualSeason(): Int? = withContext(IO)
    {
        return@withContext try
        {
            val league = LeagueDAO().findById(leagueId).toObject<League>()
            league?.actualSeason
        }
        catch(ex: Exception) { null }
    }

    suspend fun changeSeason(): Boolean = withContext(IO)
    {
        return@withContext try
        {
            LeagueDAO().update(leagueId, mapOf(actualSeason to DateUtil.actualYear.toInt() + 1))
            true
        }
        catch(ex: Exception) { false }
     }


    private const val actualSeason = "actualSeason"
    private const val leagueId = "league"
}