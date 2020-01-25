package cz.prague.cvut.fit.steuejan.amtelapp.states

sealed class NameState
data class ValidName(val name: String) : NameState()
data class InvalidName(val errorMessage: String) : NameState()