package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R

sealed class PasswordState
{
    companion object
    {
        fun validate(password: String): PasswordState
        {
            return when(password.length)
            {
                0 -> InvalidPassword(App.context.getString(R.string.password_invalid_empty_message))
                in 1..5 -> InvalidPassword(App.context.getString(R.string.password_invalid_short_message))
                else -> ValidPassword(password)
            }
        }
    }

}
data class ValidPassword(val self: String = "") : PasswordState()
data class InvalidPassword(val errorMessage: String = "") : PasswordState()
