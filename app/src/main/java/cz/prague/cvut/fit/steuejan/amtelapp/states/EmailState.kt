package cz.prague.cvut.fit.steuejan.amtelapp.states

sealed class EmailState
data class ValidEmail(val email: String) : EmailState()
data class InvalidEmail(val errorMessage: String) : EmailState()