@file:Suppress("LocalVariableName")

package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.Message
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.firstLetterUpperCase
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Day
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toDayInWeek
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch

class AccountTMMakeTeamFragmentVM : ViewModel()
{
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

    private val _teamUsers = MutableLiveData<List<User>>()
    val teamUsers: LiveData<List<User>> = _teamUsers

    /*---------------------------------------------------*/

    fun createTeam(user: User, name: String, place: String, days: String)
    {
        if(confirmInput(name, place, days))
        {
            viewModelScope.launch {
                val _days = playingDaysState.value as ValidPlayingDays

                val users = mutableListOf<User>().apply {
                    user.teamId?.let {
                        val team = TeamManager.findTeam(it)
                        if(team is ValidTeam)
                        {
                            if(team.self.users.isEmpty()) add(user)
                            else addAll(team.self.users)
                        }
                    } ?: let {
                        add(user)
                        _teamUsers.value = listOf(user)
                    }

                }

                var team: Team? = Team(
                    user.teamId,
                    name,
                    AuthManager.currentUser!!.uid,
                    _days.self,
                    place.firstLetterUpperCase(),
                    users.map { it.id!! }.toMutableList(),
                    users
                )

                team = TeamManager.addTeam(team!!)

                if(team != null) teamState.value = ValidTeam(team)
                else teamState.value = NoTeam
            }
        }
    }

    fun setTeamUsers(team: Team)
    {
        viewModelScope.launch {
            _teamUsers.value = team.users
        }
    }

    fun updateUser(user: User, team: Team)
    {
        viewModelScope.launch {
            UserManager.updateUser(user.id!!, mapOf(
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

        return Message(title, null)
    }

    private fun confirmInput(name: String, place: String, playingDays: String): Boolean
    {
        var okName = true
        var okPlace = true
        var okDays = true

        if(name.isEmpty())
        {
            nameState.value = InvalidName()
            okName = false
        }

        if(place.isEmpty())
        {
            placeState.value = InvalidPlace()
            okPlace = false
        }

        if(playingDays.isEmpty())
        {
            playingDaysState.value = InvalidPlayingDays()
            okDays = false
        }
        else playingDaysState.value = ValidPlayingDays(playingDays.split(","))

        return okName && okPlace && okDays
    }

    fun setDialogDays(days: Editable): IntArray
    {
        return if(days.isEmpty()) intArrayOf()
        else days.toString().split(Regex(",[ ]+")).map { day -> day.toDayInWeek().ordinal }.toIntArray()
    }

    fun getDialogDays(items: List<CharSequence>): List<Day> =
        items.map { it.toString().toDayInWeek() }.sortedBy { it.ordinal }

}