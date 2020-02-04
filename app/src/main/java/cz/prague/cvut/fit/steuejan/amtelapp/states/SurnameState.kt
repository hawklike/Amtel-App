package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R

sealed class SurnameState
data class ValidSurname(val name: String) : SurnameState()
data class InvalidSurname(val errorMessage: String = App.context.getString(R.string.invalidSurname_error)) : SurnameState()