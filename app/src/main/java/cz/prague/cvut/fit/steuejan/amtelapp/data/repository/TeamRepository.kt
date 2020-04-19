package cz.prague.cvut.fit.steuejan.amtelapp.data.repository

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SearchPreparation
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.StringUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.TestingUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.MatchDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.TeamDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserOrderBy
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object TeamRepository
{
    suspend fun setTeam(team: Team): Team? = withContext(IO)
    {
        return@withContext try
        {
            val search = SearchPreparation(team.name)
            team.searchNameComplete = search.preparedText
            team.searchName = search.removeSportClubAcronym()

            team.englishName = StringUtil.prepareCzechOrdering(team.name)

            TeamDAO().insert(team)
            Log.d(TAG, "setTeam(): team $team successfully set/updated in database")
            team
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "setTeam(): team $team not set/updated in database because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::setTeam(): team $team not set/updated in database because ${ex.message}")
                throwNonFatal(ex)
            }
            null
        }
    }

    suspend fun updateTeam(documentId: String?, mapOfFieldsAndValues: Map<String, Any?>): Boolean = withContext(IO)
    {
        if(documentId == null) return@withContext false
        return@withContext try
        {
            TeamDAO().update(documentId, mapOfFieldsAndValues)
            Log.d(TAG, "updateTeam(): team with id $documentId successfully updated with $mapOfFieldsAndValues")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "updateTeam(): team with id $documentId not updated with $mapOfFieldsAndValues because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::updateTeam(): team with id $documentId not updated with $mapOfFieldsAndValues because ${ex.message}")
                throwNonFatal(ex)
            }
            false
        }
    }

    suspend fun findTeam(id: String?): TeamState = withContext(IO)
    {
        if(id == null) return@withContext NoTeam
        return@withContext try
        {
            val team = TeamDAO().findById(id).toObject<Team>()
            Log.d(TAG, "findTeam(): $team found in database")
            team?.let { ValidTeam(team) } ?: NoTeam
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findTeam(): team with id $id not found in database because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::findTeam(): team with id $id not found in database because ${ex.message}")
                throwNonFatal(ex)
            }
            NoTeam
        }
    }

    suspend fun <T> findTeams(field: String, value: T?): TeamState = withContext(IO)
    {
        return@withContext try
        {
            val teams = TeamDAO().find(field, value).toObjects<Team>()
            Log.d(TAG, "findTeams(): $teams where $field is $value found successfully")
            ValidTeams(teams)
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "findTeams(): documents where $field is $value not found because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::findTeams(): documents where $field is $value not found because ${ex.message}")
                throwNonFatal(ex)
            }
            NoTeam
        }
    }

    fun retrieveAllTeamsQuery(): Query
            = TeamDAO().retrieveAllTeams()

    suspend fun retrieveAllTeams(): List<Team> = withContext(IO)
    {
        return@withContext try
        {
            retrieveAllTeamsQuery()
                .get()
                .await()
                .toObjects<Team>()
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "retrieveAllTeams: teams not retrieved because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::retrieveAllTeams: teams not retrieved because ${ex.message}")
                throwNonFatal(ex)
            }
            emptyList<Team>()
        }
    }

    suspend fun updateUserInTeam(updatedUser: User?): Boolean = withContext(IO)
    {
        updatedUser ?: return@withContext false
        return@withContext try
        {
            TeamDAO().updateUser(updatedUser)
            Log.d(TAG, "updateUserInTeam(): $updatedUser successfully updated")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "updateUserInTeam(): user $updatedUser not updated because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::updateUserInTeam(): user $updatedUser not updated because ${ex.message}")
                throwNonFatal(ex)
            }
            false
        }
    }

    fun retrieveMatches(team: Team): Query = MatchDAO().getMatches(team.id!!)

    fun retrieveAllUsers(teamId: String, orderBy: UserOrderBy = UserOrderBy.SURNAME): Query
    {
        var query: Query? = null
        UserOrderBy.values().forEach {
            if(orderBy == it) query = TeamDAO().retrieveAllUsers(it.toString(), teamId)
        }
        return query!!
    }

    suspend fun retrieveTeamsInSeason(groupId: String?, year: Int): TeamState = withContext(IO)
    {
        if(groupId == null) return@withContext NoTeam
        return@withContext try
        {
            val group = GroupRepository.findGroup(groupId)
            if(group is ValidGroup)
            {
                val teams = mutableListOf<Team>()
                group.self.teamIds[year.toString()]?.let { teamIds ->
                    teamIds.forEach { teamId ->
                        with(findTeam(teamId)) {
                            if(this is ValidTeam) teams.add(self)
                        }
                    }
                }
                Log.d(TAG, "retrieveTeamsInSeason(): $teams in group with id $groupId and year $year found successfully")
                ValidTeams(teams)
            }
            else NoTeam
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "retrieveTeamsInSeason(): teams in group with id $groupId and year $year not found because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::retrieveTeamsInSeason(): teams in group with id $groupId and year $year not found because ${ex.message}")
                throwNonFatal(ex)
            }
            NoTeam
        }
    }

    fun retrieveTeamsByPrefix(textToSearch: String): Pair<Query, Boolean>
    {
        val preparation = SearchPreparation(textToSearch)
        val doCompleteSearch = preparation.doCompleteSearch(textToSearch)
        val searchField =
            if(doCompleteSearch) searchNameComplete
            else searchName
        return Pair(TeamDAO().retrieveTeamsByPrefix(preparation.preparedText, searchField), doCompleteSearch)
    }

    private const val TAG = "TeamRepository"
    const val groupName = "groupName"
    const val groupId = "groupId"
    const val searchName = "searchName"
    const val searchNameComplete = "searchNameComplete"
}