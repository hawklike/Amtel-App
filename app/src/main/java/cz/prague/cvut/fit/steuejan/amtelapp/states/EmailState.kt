package cz.prague.cvut.fit.steuejan.amtelapp.states

sealed class EmailState
data class ValidEmail(val email: String) : EmailState()
object InvalidEmail : EmailState()