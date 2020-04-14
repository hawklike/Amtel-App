package cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AbstractBaseActivityVM : ViewModel()
{
    private val _logoutIcon = MutableLiveData<Boolean>()

    fun setLogoutIcon(visible: Boolean)
    {
        _logoutIcon.value = visible
    }

    val logoutIcon: LiveData<Boolean> = _logoutIcon
}
