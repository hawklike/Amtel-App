package cz.prague.cvut.fit.steuejan.amtelapp.authentication

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import cz.prague.cvut.fit.steuejan.amtelapp.R

object AuthUtil
{
    private val auth by lazy { FirebaseAuth.getInstance() }

    fun getProfileDrawerOption(context: Context): String =
        auth.currentUser?.let { context.getString(R.string.account) }
            ?: context.getString(R.string.login)
}