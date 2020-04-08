@file:Suppress("MemberVisibilityCanBePrivate")

package cz.prague.cvut.fit.steuejan.amtelapp.business.util

import android.preference.PreferenceManager
import android.util.Log
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.AESCrypt

object EmailSender
{
    fun sendEmail(mailTo: String, subject: String, message: String)
    {
        password?.let {
            BackgroundMail.newBuilder(context)
                .withUsername(USERNAME)
                .withPassword(AESCrypt.decrypt(it))
                .withMailTo(mailTo)
                .withType(BackgroundMail.TYPE_PLAIN)
                .withSubject(subject)
                .withBody(message)
                .withOnSuccessCallback(object : BackgroundMail.OnSendingCallback
                {
                    override fun onSuccess()
                    {
                        Log.i(TAG, "sendEmail(): email to $mailTo successfully sent")
                    }

                    override fun onFail(ex: Exception)
                    {
                        Log.e(TAG, "sendEmail(): email to $mailTo not sent because ${ex.message}")
                    }
                })
                .send()

        } ?: let { Log.e(TAG, "sendEmail(): password not found") }
    }

    private val password: String?
    get()
    {
        return PreferenceManager
            .getDefaultSharedPreferences(context)
            ?.getString(context.getString(R.string.email_password_key), null)
    }

    var hasPassword = false
    var headOfLeagueEmail: String? = null
    private const val TAG = "EmailSender"
    private const val USERNAME = "noreply.amtelopava@gmail.com"
}