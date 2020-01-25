package cz.prague.cvut.fit.steuejan.amtelapp.states

sealed class PhoneNumberState
data class ValidPhoneNumber(val phoneNumber: String) : PhoneNumberState()
object InvalidPhoneNumber : PhoneNumberState()