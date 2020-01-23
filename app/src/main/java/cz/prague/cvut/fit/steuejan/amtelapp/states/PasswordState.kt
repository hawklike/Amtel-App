package cz.prague.cvut.fit.steuejan.amtelapp.states

sealed class PasswordState
data class InvalidPassword(val errorMessage: String = "") : PasswordState()
data class ValidPassword(val self: String = "") : PasswordState()