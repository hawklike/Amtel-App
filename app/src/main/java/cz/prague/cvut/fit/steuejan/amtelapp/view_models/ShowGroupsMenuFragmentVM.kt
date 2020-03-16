package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroups
import kotlinx.coroutines.launch

class ShowGroupsMenuFragmentVM : ViewModel()
{
    private val _groups = MutableLiveData<ValidGroups>()
    val groups: LiveData<ValidGroups> = _groups

    /*---------------------------------------------------*/

    fun loadGroups()
    {
        viewModelScope.launch {
            val groups = GroupManager.retrieveAllGroups()
            if(groups is ValidGroups) _groups.value = groups
        }
    }
}
