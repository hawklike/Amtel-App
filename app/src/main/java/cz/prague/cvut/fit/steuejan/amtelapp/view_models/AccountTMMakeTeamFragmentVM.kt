package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.Message
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.NameConverter
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch

class AccountTMMakeTeamFragmentVM : ViewModel()
{
    private val name = MutableLiveData<NameState>()
    fun confirmName(): LiveData<NameState> = name

    /*---------------------------------------------------*/

    private val place = MutableLiveData<PlaceState>()
    fun confirmPlace(): LiveData<PlaceState> = place

    /*---------------------------------------------------*/

    private val playingDays = MutableLiveData<PlayingDaysState>()
    fun confirmPlayingDays(): LiveData<PlayingDaysState> = playingDays

    /*---------------------------------------------------*/

    private val team = SingleLiveEvent<TeamState>()
    fun isTeamCreated(): LiveData<TeamState> = team

    /*---------------------------------------------------*/

    fun createTeam(user: User, name: String, place: String, playingDays: String, errorName: String, errorPlace: String, errorDays: String)
    {
        if(confirmInput(name, place, playingDays, errorName, errorPlace, errorDays))
        {
            viewModelScope.launch {
                val days = this@AccountTMMakeTeamFragmentVM.playingDays.value as ValidPlayingDays
                val team = TeamManager.addTeam(user.teamId, name, AuthManager.currentUser!!.uid, days.self, NameConverter.convertToFirstLetterBig(place))
                if(team != null) this@AccountTMMakeTeamFragmentVM.team.value = ValidTeam(team)
                else this@AccountTMMakeTeamFragmentVM.team.value = NoTeam
            }
        }
    }

    fun displayAfterDialog(teamState: TeamState, user: User, successTitle: String, failureTitle: String, actualizationTitle: String): Message
    {
        val title: String = if(teamState is ValidTeam) user.teamId?.let { actualizationTitle } ?: successTitle
        else failureTitle

        return Message(title, null)
    }

    private fun confirmInput(name: String, place: String, playingDays: String, errorName: String, errorPlace: String, errorDays: String): Boolean
    {
        var okName = true
        var okPlace = true
        var okDays = true

        if(name.isEmpty())
        {
            this.name.value = InvalidName(errorName)
            okName = false
        }

        if(place.isEmpty())
        {
            this.place.value = InvalidPlace(errorPlace)
            okPlace = false
        }

        if(playingDays.isEmpty())
        {
            this.playingDays.value = InvalidPlayingDays(errorDays)
            okDays = false
        }
        else this.playingDays.value = ValidPlayingDays(playingDays.split(","))

        return okName && okPlace && okDays
    }

}