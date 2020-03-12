package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.ViewModel
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import java.util.*

class ShowGroupsMenuFirestoreAdapterVM : ViewModel()
{
    fun createLabel(group: Group): CharSequence
    {
        return group.name.fold(StringBuilder()) { acc, c ->
            if(acc.length > 2) return@fold acc
            acc.append(c)
        }
    }

    fun getActualRound(group: Group): Int?
    {
        val week = DateUtil.getWeekNumber(Date())
        return try { group.roundDates.filterValues { it == week }.keys.first().toInt() }
        catch(ex: Exception) { null}
    }

}
