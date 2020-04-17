package cz.prague.cvut.fit.steuejan.amtelapp.data.repository

import cz.prague.cvut.fit.steuejan.amtelapp.business.util.TestingUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.EmailDAO
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object EmailRepository
{
    suspend fun getPassword(): String? = withContext(IO)
    {
        return@withContext try
        {
            EmailDAO().getPassword().data?.get("password")?.toString()
        }
        catch(ex: Exception)
        {
            with(TestingUtil) {
                log("EmailRepository::getPassword() failed because ${ex.message}")
                throwNonFatal(ex)
            }
            null
        }
    }
}