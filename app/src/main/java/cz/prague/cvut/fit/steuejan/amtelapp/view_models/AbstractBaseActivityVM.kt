package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AbstractBaseActivityVM() : ViewModel()
{
    private val logoutIconVisibility = MutableLiveData<Boolean>()

    fun setLogoutIconVisibility(visible: Boolean)
    {
        logoutIconVisibility.value = visible
    }

    fun getLogoutIconVisibility(): LiveData<Boolean> = logoutIconVisibility
}
