package cz.prague.cvut.fit.steuejan.amtelapp.states

sealed class CredentialsState
data class ValidCredentials(val name: String, val surname: String, val email: String) : CredentialsState()
data class InvalidCredentials(val name: Boolean, val surname: Boolean, val email: Boolean) : CredentialsState()