@file:Suppress("LocalVariableName")

package cz.prague.cvut.fit.steuejan.amtelapp.view_models

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
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.NameConverter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch

class AccountTMMakeTeamFragmentVM : ViewModel()
{
    private val nameState = MutableLiveData<NameState>()
    fun confirmName(): LiveData<NameState> = nameState

    /*---------------------------------------------------*/

    private val placeState = MutableLiveData<PlaceState>()
    fun confirmPlace(): LiveData<PlaceState> = placeState

    /*---------------------------------------------------*/

    private val playingDaysState = MutableLiveData<PlayingDaysState>()
    fun confirmPlayingDays(): LiveData<PlayingDaysState> = playingDaysState

    /*---------------------------------------------------*/

    private val teamState = SingleLiveEvent<TeamState>()
    fun isTeamCreated(): LiveData<TeamState> = teamState

    /*---------------------------------------------------*/

    private val teamUsers = MutableLiveData<List<User>>()
    fun getTeamUsers(): LiveData<List<User>> = teamUsers

    /*---------------------------------------------------*/

    fun createTeam(user: User, name: String, place: String, days: String)
    {
        if(confirmInput(name, place, days))
        {
            viewModelScope.launch {
                val _days = playingDaysState.value as ValidPlayingDays

                val usersId = mutableListOf<String>().apply {
                    user.teamId?.let {
                        val team = TeamManager.findTeam(it)
                        if(team is ValidTeam)
                        {
                            if(team.self.usersId.isEmpty()) this.add(user.id!!)
                            else this.addAll(team.self.usersId)
                        }
                    } ?: let {
                        this.add(user.id!!)
                        teamUsers.value = listOf(user)
                    }

                }

                val team = TeamManager.addTeam(
                    user.teamId,
                    name,
                    AuthManager.currentUser!!.uid,
                    _days.self,
                    NameConverter.convertToFirstLetterBig(place),
                    usersId)

                if(team != null) teamState.value = ValidTeam(team)
                else teamState.value = NoTeam
            }
        }
    }

    fun setTeamUsers(team: Team)
    {
        viewModelScope.launch {
            teamUsers.value = UserManager.findUsers(team.usersId)
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

}