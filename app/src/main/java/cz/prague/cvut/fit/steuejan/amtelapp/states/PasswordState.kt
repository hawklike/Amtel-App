package cz.prague.cvut.fit.steuejan.amtelapp.states

sealed class PasswordState
object InvalidPassword : PasswordState()
object ValidPassword : PasswordState()