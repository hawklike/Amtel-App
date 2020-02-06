package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R

sealed class PlayingDaysState
data class ValidPlayingDays(val self: List<String>) : PlayingDaysState()
data class InvalidPlayingDays(val errorMessage: String = App.context.getString(R.string.create_team_failure_days_error)) : PlayingDaysState()