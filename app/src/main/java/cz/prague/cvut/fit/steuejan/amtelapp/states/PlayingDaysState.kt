package cz.prague.cvut.fit.steuejan.amtelapp.states

sealed class PlayingDaysState
data class ValidPlayingDays(val self: List<String>) : PlayingDaysState()
data class InvalidPlayingDays(val errorMessage: String) : PlayingDaysState()