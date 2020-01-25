package cz.prague.cvut.fit.steuejan.amtelapp.states

sealed class PhoneNumberState
data class ValidPhoneNumber(val self: String) : PhoneNumberState()
object InvalidPhoneNumber : PhoneNumberState()