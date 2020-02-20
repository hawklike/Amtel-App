package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group

sealed class GroupState
data class ValidGroup(val self: Group) : GroupState()
data class ValidGroups(val self: List<Group>) : GroupState()
object NoGroup : GroupState()