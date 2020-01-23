package cz.prague.cvut.fit.steuejan.amtelapp.states

sealed class RegistrationState
data class ValidRegistration(val uid: String, val password: String, val credentials: ValidCredentials) : RegistrationState()
object InvalidRegistration : RegistrationState()