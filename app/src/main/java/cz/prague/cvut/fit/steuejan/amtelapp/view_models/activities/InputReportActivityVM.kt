package cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Report
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.ReportRepository
import kotlinx.coroutines.launch

class InputReportActivityVM : ViewModel()
{
    var reportId: String? = null
    var report: Report? = null

    var published: Boolean = false
        private set

    /*---------------------------------------------------*/

//    private val _reportSaved = SingleLiveEvent<Boolean>()
//    val reportSaved: LiveData<Boolean> = _reportSaved

    /*---------------------------------------------------*/

    private val _reportPublished = SingleLiveEvent<Boolean>()
    val reportPublished: LiveData<Boolean> = _reportPublished

    /*---------------------------------------------------*/

//    fun saveReport(title: String, lead: String, text: String)
//    {
//        viewModelScope.launch {
//            ReportRepository.setReport(Report(reportId, title, lead, text))?.let {
//                reportId = it.id
//                _reportSaved.value = true
//            }
//            ?: let { _reportSaved.value = false }
//        }
//    }

    fun publishRecord(title: String, lead: String, text: String)
    {
        viewModelScope.launch {
            ReportRepository.setReport(Report(reportId, title, lead, text))?.let {
                reportId = it.id
                published = true
                _reportPublished.value = true
            } ?: let { _reportPublished.value = false }
        }
    }
}
