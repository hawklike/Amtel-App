package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
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
            Log.i("LeagueManager", "getActualSeason(): ${league?.actualSeason} successfully retrieved from database")
            league?.actualSeason
        }
        catch(ex: Exception)
        {
            Log.e("LeagueManager", "getActualSeason(): actual season not retrieved from database because ${ex.message}")
            null
        }
    }

    suspend fun changeSeason(): Boolean = withContext(IO)
    {
        return@withContext try
        {
            LeagueDAO().update(leagueId, mapOf(actualSeason to DateUtil.actualSeason.toInt() + 1))
            DateUtil.actualSeason = (DateUtil.actualSeason.toInt() + 1).toString()
            true
        }
        catch(ex: Exception) { false }
     }


    private const val actualSeason = "actualSeason"
    private const val leagueId = "league"
}