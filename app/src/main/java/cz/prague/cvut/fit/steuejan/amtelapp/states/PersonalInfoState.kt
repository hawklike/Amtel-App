package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Sex

sealed class PersonalInfoState
data class PersonalInfoSuccess(val name: String, val surname: String, val birthdate: String, val phoneNumber: String?, val sex: Sex) : PersonalInfoState()
object PersonalInfoFailure : PersonalInfoState()