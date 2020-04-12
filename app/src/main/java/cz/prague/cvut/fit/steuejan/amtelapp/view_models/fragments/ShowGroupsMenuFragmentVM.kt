package cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.GroupRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroups
import kotlinx.coroutines.launch

class ShowGroupsMenuFragmentVM : ViewModel()
{
    private val _groups = SingleLiveEvent<List<Group>>()
    val groups: LiveData<List<Group>> = _groups

    /*---------------------------------------------------*/

    fun loadGroups()
    {
        viewModelScope.launch {
            val groups = GroupRepository.retrieveAllGroups()
            if(groups is ValidGroups) _groups.value = groups.self.filter { it.visibility }
        }
    }
}
