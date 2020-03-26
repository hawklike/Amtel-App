package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Message
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AccountBossMakeGroupsFragmentVM : ViewModel()
{
    private val groupNameState = MutableLiveData<NameState>()
    val groupName: LiveData<NameState> = groupNameState

    /*---------------------------------------------------*/

    private val _group = SingleLiveEvent<GroupState>()
    val group: LiveData<GroupState> = _group

    /*---------------------------------------------------*/

    private val allGroups = MutableLiveData<List<Group>>()

    private fun setAllGroups(groups: List<Group>)
    {
        Log.i("AccountBossMakeGroups", "$groups added")
        allGroups.value = groups
    }

    fun getAllGroups(): LiveData<List<Group>> = allGroups

    /*---------------------------------------------------*/

    fun createGroup(groupName: String, playingPlayOff: Boolean)
    {
        if(confirmName(groupName))
        {
            viewModelScope.launch {
                val groups = GroupManager.findGroups("name", groupName)
                if(groups is ValidGroups && groups.self.isNotEmpty())
                {
                    _group.value = NoGroup
                    return@launch
                }
                _group.value = GroupManager.addGroup(Group(null, groupName, playingPlayOff = playingPlayOff)).let {
                    if(it is ValidGroup) ValidGroup(it.self)
                    else NoGroup
                }
            }
        }
    }

    fun getGroups()
    {
        viewModelScope.launch {
            val groups = GroupManager.retrieveAllGroupsExceptPlayOff()
                if(groups is ValidGroups) setAllGroups(groups.self)
        }
    }

    fun updateGroups(group: GroupState)
    {
        if(group is ValidGroup)
        {
            val groups = getAllGroups().value?.toMutableList() ?: mutableListOf()
            groups.add(group.self)
            setAllGroups(groups.toList())
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

    fun displayDialog(group: GroupState): Message
    {
        val successTitle = context.getString(R.string.create_group_success_title)
        val failureTitle = context.getString(R.string.create_group_failure_title)

        return if(group is ValidGroup) Message(
            successTitle,
            null
        )
        else Message(
            failureTitle,
            null
        )
    }

    fun updateRanks(groups: List<Group>)
    {
        GlobalScope.launch {
            for(i in groups.indices)
                GroupManager.updateGroup(groups[i].id!!, mapOf("rank" to i))
        }
    }

}