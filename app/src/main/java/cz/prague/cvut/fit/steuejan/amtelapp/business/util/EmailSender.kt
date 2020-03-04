package cz.prague.cvut.fit.steuejan.amtelapp.business.util

import android.preference.PreferenceManager
import android.util.Log
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.AESCrypt

object EmailSender
{
    fun sendVerificationEmail(mailTo: String, genPassword: String)
    {
        var password = PreferenceManager
            .getDefaultSharedPreferences(context)
            ?.getString(context.getString(R.string.email_password_key), "")

        if(password?.length == 0) password = null

        password?.let {
            BackgroundMail.newBuilder(context)
                .withUsername("noreply.amtelopava@gmail.com")
                .withPassword(AESCrypt.decrypt(it))
                .withMailTo(mailTo)
                .withType(BackgroundMail.TYPE_PLAIN)
                .withSubject(context.getString(R.string.verificationEmail_subject))
                .withBody(createVerificationTemplate(mailTo, genPassword))
                .withOnSuccessCallback(object : BackgroundMail.OnSendingCallback
                {
                    override fun onSuccess()
                    {
                        Log.i(TAG, "sendVerificationEmail(): email successfully sent")
                    }

                    override fun onFail(e: Exception)
                    {
                        Log.e(TAG, "sendVerificationEmail(): email not sent because ${e.message}")
                    }
                })
                .send()
        }
    }

   private fun createVerificationTemplate(email: String, password: String): String
   {
       val head = context.getString(R.string.autoEmail_template_head)
       val body = "email: $email\nheslo: $password\n\n"
       val foot = context.getString(R.string.autoEmail_template_foot)
       return "$head$body$foot"
   }

    var hasPassword = false
    private const val TAG = "EmailSender"
}