package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Playoff
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toPlayoff
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroup
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroups
import kotlinx.coroutines.launch

class ManageGroupsActivityVM : ViewModel()
{
    var playoff: Playoff? = null
        private set

    /*---------------------------------------------------*/

    private val _groups = MutableLiveData<List<Group>>()
    val groups: LiveData<List<Group>> = _groups

    /*---------------------------------------------------*/

    private val _isPlayOffOpen = MutableLiveData<Boolean>()
    val isPlayOffOpen: LiveData<Boolean> = _isPlayOffOpen

    /*---------------------------------------------------*/

    fun setPlayOff()
    {
        viewModelScope.launch {
            val playOff = Group(null, context.getString(R.string.playOff), playingPlayOff = false, playOff = true, rank = Int.MAX_VALUE)
            if(GroupManager.addPlayoff(playOff) is ValidGroup) toast("Baráž úspěšně otevřena.")
        }
    }

    fun getGroupsExceptPlayOff()
    {
        viewModelScope.launch {
            val groups = GroupManager.retrieveAllGroupsExceptPlayoff()
            if(groups is ValidGroups) _groups.value = groups.self
        }
    }

    fun getPlayoff()
    {
        viewModelScope.launch {
            val results = GroupManager.findGroups("playOff", true)
            if(results is ValidGroups && results.self.isNotEmpty())
            {
                val playOff = results.self.first().toPlayoff()
                this@ManageGroupsActivityVM.playoff = playOff
                _isPlayOffOpen.value = playOff?.isActive ?: false
            }
            else
            {
                playoff = null
                _isPlayOffOpen.value = false
            }
        }
    }

}
