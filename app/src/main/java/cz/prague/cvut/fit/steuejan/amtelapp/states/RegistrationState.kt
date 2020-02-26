package cz.prague.cvut.fit.steuejan.amtelapp.states

sealed class RegistrationState
object ValidRegistration : RegistrationState()
object InvalidRegistration : RegistrationState()