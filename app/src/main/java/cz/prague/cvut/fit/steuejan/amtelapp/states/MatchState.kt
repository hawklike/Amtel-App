package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Match

sealed class MatchState
data class ValidMatch(val self: Match) : MatchState()
object NoMatch : MatchState()