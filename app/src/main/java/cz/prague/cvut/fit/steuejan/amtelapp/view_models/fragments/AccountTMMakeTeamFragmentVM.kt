@file:Suppress("LocalVariableName")

package cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.LeagueRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.UserRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Day
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Message
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toDayInWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.*

class AccountTMMakeTeamFragmentVM : ViewModel()
{
    private var deadline: Pair<Date?, Date?>? = null
    var deadlineDialog: String = ""

    /*---------------------------------------------------*/

    private val nameState = MutableLiveData<NameState>()
    val name: LiveData<NameState> = nameState

    /*---------------------------------------------------*/

    private val placeState = MutableLiveData<PlaceState>()
    val place: LiveData<PlaceState> = placeState

    /*---------------------------------------------------*/

    private val playingDaysState = MutableLiveData<PlayingDaysState>()
    val playingDays: LiveData<PlayingDaysState> = playingDaysState

    /*---------------------------------------------------*/

    private val teamState = SingleLiveEvent<TeamState>()
    val newTeam: LiveData<TeamState> = teamState

    /*---------------------------------------------------*/

    private val _team = MutableLiveData<Team>()
    val team: LiveData<Team> = _team

    /*---------------------------------------------------*/

    private val _isLineUpAllowed = MutableLiveData<Boolean>()
    val isLineUpAllowed: LiveData<Boolean> = _isLineUpAllowed

    /*---------------------------------------------------*/

    /*
    This method creates a new team, if not created, or updates an existing team.
     */
    fun createTeam(user: User, name: String, place: String, days: String)
    {
        if(confirmInput(name, days))
        {
            viewModelScope.launch {
                val _days = playingDaysState.value as ValidPlayingDays

                val users = mutableListOf<User>().apply {
                    user.teamId?.let {
                        //team already created
                        val team = TeamRepository.findTeam(it)
                        if(team is ValidTeam)
                        {
                            //team has no users, add team manager
                            if(team.self.users.isEmpty()) add(user)
                            //team already has users, copy them (add them to list)
                            else addAll(team.self.users)
                        }
                    } ?: add(user) //team not created yet, add team manager among future team players
                }

                //create/update team
                var team: Team? = Team(
                    user.teamId,
                    name,
                    AuthManager.currentUser!!.uid,
                    _days.self,
                    if(place.isEmpty()) null else place,
                    users.map { it.id!! }.toMutableList(),
                    users
                )

                //set team in database
                team = TeamRepository.setTeam(team!!)

                //all ok?
                if(team != null) teamState.value = ValidTeam(team)
                else teamState.value = NoTeam
            }
        }
    }

    fun updateUser(user: User, team: Team)
    {
        //user has a (new) team
        viewModelScope.launch {
            UserRepository.updateUser(user.id, mapOf(
                "teamId" to user.teamId,
                "teamName" to team.name))
        }
    }

    fun displayAfterDialog(teamState: TeamState, user: User): Message
    {
        val successTitle = App.context.getString(R.string.add_team_success_title)
        val failureTitle = App.context.getString(R.string.add_team_failure_title)
        val actualizationTitle = App.context.getString(R.string.add_team_actualization_title)

        val title: String = if(teamState is ValidTeam) user.teamId?.let { actualizationTitle } ?: successTitle
        else failureTitle

        return Message(
            title,
            null
        )
    }

    fun isLineUpAllowed()
    {
        if(DateUtil.serverTime == null)
        {
            viewModelScope.launch {
                //get actual time from server
                LeagueRepository.getServerTime()?.let {
                    //keep that time until next visit
                    DateUtil.serverTime = it
                    isLineUpAllowed(it)
                }
            }
        }
        else isLineUpAllowed(DateUtil.serverTime)
    }

    private fun confirmInput(name: String, playingDays: String): Boolean
    {
        var isOk = true

        if(name.isEmpty())
        {
            nameState.value = InvalidName()
            isOk = false
        }

        if(playingDays.isEmpty())
        {
            playingDaysState.value = InvalidPlayingDays()
            isOk = false
        }
        else playingDaysState.value = ValidPlayingDays(playingDays.split(",").map { it.trim() })

        return isOk
    }

    fun setDialogDays(days: Editable): IntArray
    {
        return if(days.isEmpty()) intArrayOf()
        else days.toString().split(Regex(",[ ]+")).map { day -> day.toDayInWeek().ordinal }.toIntArray()
    }

    fun getDialogDays(items: List<CharSequence>): List<Day> =
        items.map { it.toString().toDayInWeek() }.sortedBy { it.ordinal }

    private fun isLineUpAllowed(serverTime: Date?)
    {
        //deadline not set yet
        if(deadline == null)
        {
            viewModelScope.launch {
                //retrieve deadline from database
                LeagueRepository.getDeadline()?.let { deadlineRange ->
                    val from = deadlineRange.first
                    val to = deadlineRange.second
                    //update variable
                    deadline = deadlineRange
                    _isLineUpAllowed.value = isLineUpAllowed(serverTime, from, to)
                }
            }
        }
        else
        {
            //deadline is already set
            val from = deadline?.first
            val to = deadline?.second
            _isLineUpAllowed.value = isLineUpAllowed(serverTime, from, to)
        }
    }

    private fun isLineUpAllowed(serverTime: Date?, from: Date?, to: Date?): Boolean
    {
        return if(serverTime == null) false
        else if(from == null && to == null)
        {
            deadlineDialog = "Termín uzavření soupisky není dosud stanoven."
            true
        }
        else if(from == null && to != null)
        {
            deadlineDialog = "Tvorba soupisky je uzavřena do ${to.toMyString()}."
            !DateUtil.isDateBetween(serverTime, Date(0), to)
        }
        else if(from != null && to == null)
        {
            deadlineDialog = "Tvorba soupisky je uzavřena od ${from.toMyString()}."
            !DateUtil.isDateBetween(serverTime, from, DateTime().plusYears(30).toDate())
        }
        else
        {
            deadlineDialog = "Tvorba soupisky je uzavřena od ${from?.toMyString()} do ${to?.toMyString()}."
            !DateUtil.isDateBetween(serverTime, from, to)
        }
    }

    fun getUpdatedTeam(team: TeamState)
    {
        if(team is ValidTeam)
        {
            viewModelScope.launch {
                TeamRepository.findTeam(team.self.id).let {
                    if(it is ValidTeam) _team.value = it.self
                }
            }
        }
    }

}