package cz.prague.cvut.fit.steuejan.amtelapp.business.managers

import android.util.Log
import com.google.firebase.firestore.ktx.toObject
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.LeagueDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.League
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.util.*

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

    suspend fun setDeadline(deadline: Date): Boolean = withContext(IO)
    {
        return@withContext try
        {
            val dao = LeagueDAO()
            val league = dao.findById(leagueId).toObject<League>()
            if(league != null)
            {
                league.deadline[DateUtil.actualSeason] = deadline
                dao.insert(league)
                return@withContext true
            }
            true
        }
        catch(ex: Exception) { false }
    }

    suspend fun getDeadline(): Date? = withContext(IO)
    {
        return@withContext try
        {
            val league = LeagueDAO().findById(leagueId).toObject<League>()
            league?.deadline?.get(DateUtil.actualSeason)
        }
        catch(ex: Exception) { null }
    }



    private const val actualSeason = "actualSeason"
    private const val leagueId = "league"
}