package cz.prague.cvut.fit.steuejan.amtelapp.states

import java.util.*

sealed class BirthdateState
data class ValidBirthdate(val date: Date) : BirthdateState()
data class InvalidBirthdate(val errorMessage: String) : BirthdateState()