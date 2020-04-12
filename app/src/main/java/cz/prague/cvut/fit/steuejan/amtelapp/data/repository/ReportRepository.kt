package cz.prague.cvut.fit.steuejan.amtelapp.data.repository

import android.util.Log
import cz.prague.cvut.fit.steuejan.amtelapp.data.dao.ReportDAO
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Report
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ReportRepository
{
    const val TAG = "ReportRepository"
    const val PUBLISHED = "published"

    suspend fun setReport(report: Report): Report? = withContext(Dispatchers.IO)
    {
        return@withContext try
        {
            ReportDAO().insert(report)
            Log.i(TAG, "setReport(): $report successfully added/updated to database")
            report
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "setReport(): $report not added to database because ${ex.message}")
            null
        }
    }

    suspend fun updateReport(documentId: String?, mapOfFieldsAndValues: Map<String, Any?>): Boolean = withContext(Dispatchers.IO)
    {
        if(documentId == null) return@withContext false
        return@withContext try
        {
            ReportDAO().update(documentId, mapOfFieldsAndValues)
            Log.i(TAG, "updateReport(): report with id $documentId successfully updated with $mapOfFieldsAndValues")
            true
        }
        catch(ex: Exception)
        {
            Log.e(TAG, "updateReport(): report with id $documentId not updated because ${ex.message}")
            false
        }
    }
}