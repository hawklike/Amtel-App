package cz.prague.cvut.fit.steuejan.amtelapp.view_models.adapters

import android.content.res.ColorStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.normal.ShowGroupsBossAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.GroupRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import kotlinx.coroutines.launch

class ShowGroupsBossAdapterVM : ViewModel()
{
    fun confirmInput(text: String, rounds: Int): Boolean
    {
        val n: Int

        try { n = text.toInt() }
        catch(ex: NumberFormatException) { return false }

        if(n % 2 == 0 || n < rounds) return false

        return true
    }

    fun handleVisibility(visible: Boolean, group: Group, holder: ShowGroupsBossAdapter.ViewHolder)
    {
        viewModelScope.launch {
            if(GroupRepository.updateGroup(group.id, mapOf(GroupRepository.visibility to visible)))
            {
                if(visible)
                {
                    holder.visibility.backgroundTintList = ColorStateList.valueOf(App.getColor(R.color.blue))
                    toast("Skupina ${group.name} je nyní viditelná.")
                }
                else
                {
                    holder.visibility.backgroundTintList = ColorStateList.valueOf(App.getColor(R.color.lightGrey))
                    toast("Skupina ${group.name} je nyní skrytá.")
                }
            }
        }
    }
}