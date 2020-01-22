package cz.prague.cvut.fit.steuejan.amtelapp.business.utils

import android.content.Context
import android.util.Log
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.activities.AbstractBaseActivity
import cz.prague.cvut.fit.steuejan.amtelapp.activities.AbstractBaseActivity.Companion.TAG

object EmailSender
{
    fun sendVerificationEmail(context: Context, mailTo: String, genPassword: String)
    {
        Firebase.firestore
            .collection("email_password")
            .document("noreply.amtelopava@gmail.com")
            .get()
            .addOnSuccessListener { password ->
                if(password != null)
                {
                    BackgroundMail.newBuilder(context)
                        .withUsername("noreply.amtelopava@gmail.com")
                        .withPassword(password.data?.get("password").toString())
                        .withMailTo(mailTo)
                        .withType(BackgroundMail.TYPE_PLAIN)
                        .withSubject(context.getString(R.string.verificationEmail_subject))
                        .withBody(createVerificationTemplate(mailTo, genPassword))
                        .withOnSuccessCallback(object : BackgroundMail.OnSendingCallback
                        {
                            override fun onSuccess()
                            {
                                Log.i(AbstractBaseActivity.TAG, "email sent")
                            }

                            override fun onFail(e: Exception)
                            {
                                Log.e(AbstractBaseActivity.TAG, "email not sent: ${e.message}")
                            }
                        })
                        .send()
                }
                else Log.d(TAG, "password not found")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "password failed because ", exception)
            }
    }

   private fun createVerificationTemplate(email: String, password: String): String
   {
       val head = "Dobrý den,\nbyl Vám udělen přístup do aplikace AMTEL Opava. Vaše přiřazená role je vedoucí týmu. Pro přihlášení zadejte v aplikaci následující kombinaci:\n\n"
       val body = "email: $email\nheslo: $password\n\n"
       val foot = "Heslo Vám bylo náhodně vygenerováno. Můžete si jej změnit v aplikaci.\n\nS přáním příjemného dne,\nMgr. Jiří Vaněk\n\nPoznámka: Tento email je náhodně generovaný, neodpovídejte na něj prosím."
       return "$head$body$foot"
   }
}