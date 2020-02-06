package cz.prague.cvut.fit.steuejan.amtelapp.business.util

import android.util.Log
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R

object EmailSender
{
    //TODO: refactor to a nicer code
    //TODO: hash a password
    fun sendVerificationEmail(mailTo: String, genPassword: String)
    {
        Firebase.firestore
            .collection("email_password")
            .document("noreply.amtelopava@gmail.com")
            .get()
            .addOnSuccessListener { password ->
                if(password != null)
                {
                    BackgroundMail.newBuilder(App.context)
                        .withUsername("noreply.amtelopava@gmail.com")
                        .withPassword(password.data?.get("password").toString())
                        .withMailTo(mailTo)
                        .withType(BackgroundMail.TYPE_PLAIN)
                        .withSubject(App.context.getString(R.string.verificationEmail_subject))
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
            .addOnFailureListener { exception ->
                Log.e(TAG, "sendVerificationEmail(): password not found because $exception")
            }
    }

   private fun createVerificationTemplate(email: String, password: String): String
   {
       val head = App.context.getString(R.string.autoEmail_template_head)
       val body = "email: $email\nheslo: $password\n\n"
       val foot = App.context.getString(R.string.autoEmail_template_foot)
       return "$head$body$foot"
   }

    private const val TAG = "EmailSender"
}