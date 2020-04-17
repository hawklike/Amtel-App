package cz.prague.cvut.fit.steuejan.amtelapp.data.repository

import android.util.Log
import com.google.firebase.firestore.Query
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.TestingUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.ReportDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Report
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ReportRepository
{
    const val TAG = "ReportRepository"
    const val DATE = "date"

    suspend fun setReport(report: Report): Report? = withContext(Dispatchers.IO)
    {
        return@withContext try
        {
            ReportDAO().insert(report)
            Log.d(TAG, "setReport(): report $report successfully added/updated to database")
            report
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "setReport(): report $report not added/updated to database because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::setReport(): report $report not added/updated to database because ${ex.message}")
                throwNonFatal(ex)
            }
            null
        }
    }

    suspend fun deleteReport(reportId: String?): Boolean = withContext(Dispatchers.IO)
    {
        if(reportId == null) return@withContext false
        return@withContext try
        {
            ReportDAO().delete(reportId)
            Log.d(TAG, "deleteReport(): report with id $reportId successfully deleted")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "deleteReport(): report with id $reportId not deleted because ${ex.message}")
            with(TestingUtil) {
                log("$TAG::deleteReport(): report with id $reportId not deleted because ${ex.message}")
                throwNonFatal(ex)
            }
            false
        }
    }

    fun retrieveAllReports(): Query
            = ReportDAO().retrieveAllReports()
}