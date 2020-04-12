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
    var report: Report? = null
    var published: Boolean = false

    /*---------------------------------------------------*/

    private val _reportDeleted = SingleLiveEvent<Boolean>()
    val reportDeleted: LiveData<Boolean> = _reportDeleted

    /*---------------------------------------------------*/

    private val _reportPublished = SingleLiveEvent<Boolean>()
    val reportPublished: LiveData<Boolean> = _reportPublished

    /*---------------------------------------------------*/

    fun publishRecord(id: String?, title: String, lead: String, text: String)
    {
        viewModelScope.launch {
            ReportRepository.setReport(Report(id, title, lead, text))?.let {
                published = true
                _reportPublished.value = true
            } ?: let { _reportPublished.value = false }
        }
    }

    fun deleteReport()
    {
        viewModelScope.launch {
            _reportDeleted.value = ReportRepository.deleteReport(report?.id)
        }
    }
}
