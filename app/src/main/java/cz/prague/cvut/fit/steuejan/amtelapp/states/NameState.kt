package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R

sealed class NameState
data class ValidName(val name: String) : NameState()
data class InvalidName(val errorMessage: String = App.context.getString(R.string.create_team_failure_name_error)) : NameState()