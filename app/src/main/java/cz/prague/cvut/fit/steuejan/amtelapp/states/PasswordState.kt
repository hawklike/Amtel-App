package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R

sealed class PasswordState
{
    companion object
    {
        fun validate(password: String, confirmation: String? = null): PasswordState
        {
            return when(password.length)
            {
                0 -> InvalidPassword(context.getString(R.string.password_invalid_empty_message))
                in 1..5 -> InvalidPassword(context.getString(R.string.password_invalid_short_message))
                else -> {
                    if(confirmation != null && password != confirmation) return InvalidPassword(context.getString(R.string.password_invalid_no_match))
                    return ValidPassword(password)
                }
            }
        }
    }

}
data class ValidPassword(val self: String = "") : PasswordState()
data class InvalidPassword(val errorMessage: String = "") : PasswordState()
