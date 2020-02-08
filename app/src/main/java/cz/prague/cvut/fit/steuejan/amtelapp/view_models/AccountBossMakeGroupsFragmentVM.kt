package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.Message
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidName
import cz.prague.cvut.fit.steuejan.amtelapp.states.NameState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidName
import kotlinx.coroutines.launch

class AccountBossMakeGroupsFragmentVM : ViewModel()
{
    private val groupNameState = MutableLiveData<NameState>()
    val groupName: LiveData<NameState> = groupNameState

    /*---------------------------------------------------*/

    private val _group = MutableLiveData<Group?>()
    val group: LiveData<Group?> = _group

    /*---------------------------------------------------*/

    fun createGroup(groupName: String)
    {
        if(confirmName(groupName))
        {
            viewModelScope.launch {
                _group.value = GroupManager.addGroup(Group(groupName))
            }
        }
    }

    private fun confirmName(groupName: String): Boolean
    {
        return if(groupName.isEmpty())
        {
            groupNameState.value = InvalidName(context.getString(R.string.group_name_error_message))
            false
        }
        else
        {
            groupNameState.value = ValidName(groupName)
            true
        }
    }

    fun displayDialog(group: Group?): Message
    {
        val successTitle = context.getString(R.string.create_group_success_title)
        val failureTitle = context.getString(R.string.create_group_failure_title)

        return if(group != null) Message(successTitle, null)
        else Message(failureTitle, null)
    }

}