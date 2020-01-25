package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
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


    fun createTeam(name: String, place: String, playingDays: String)
    {
        if(confirmInput(name, place, playingDays))
        {
            viewModelScope.launch {
                val days = this@AccountTMMakeTeamFragmentVM.playingDays.value as ValidPlayingDays
                val team = TeamManager.addTeam(name, AuthManager.currentUser!!.uid, days.self, place)
                if(team != null) this@AccountTMMakeTeamFragmentVM.team.value = ValidTeam(team)
                else this@AccountTMMakeTeamFragmentVM.team.value = NoTeam
            }
        }
    }

    //TODO: implement confirmation
    private fun confirmInput(name: String, place: String, playingDays: String): Boolean
    {
        this.playingDays.value = ValidPlayingDays(playingDays.split(","))
        return true
    }

}