package cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.MessageRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Message
import kotlinx.coroutines.launch

class MatchDiscussionActivityVM : ViewModel()
{
    private val _messageSent = MutableLiveData<Boolean>()
    val message: LiveData<Boolean> = _messageSent

    /*---------------------------------------------------*/

    fun addMessage(messageText: String, matchId: String?)
    {
        viewModelScope.launch {
            MessageRepository.addMessage(Message(messageText = messageText), matchId, false)?.let {
                _messageSent.value = true
            }
        }
    }
}
