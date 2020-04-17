package cz.prague.cvut.fit.steuejan.amtelapp.business.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

object TestingUtil
{
    private val crashlytics = FirebaseCrashlytics.getInstance()

    fun setCurrentUser(userId: String)
    {
        crashlytics.setUserId(userId)
    }

    fun log(message: String)
    {
        crashlytics.log(message)
    }

    fun throwNonFatal(exception: Exception)
    {
        crashlytics.recordException(exception)
    }

}