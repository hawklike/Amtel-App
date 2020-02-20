package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team

sealed class TeamState
data class ValidTeam(val self: Team) : TeamState()
data class ValidTeams(val self: List<Team>) : TeamState()
object NoTeam : TeamState()