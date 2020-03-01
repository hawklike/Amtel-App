package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidSet
import cz.prague.cvut.fit.steuejan.amtelapp.states.SetState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidSet

class MatchInputResultFragmentVM : ViewModel()
{
    private val _firstHome = MutableLiveData<SetState>()
    val firstHome: LiveData<SetState> = _firstHome

    /*---------------------------------------------------*/

    private val _firstAway = MutableLiveData<SetState>()
    val firstAway: LiveData<SetState> = _firstAway

    /*---------------------------------------------------*/

    private val _secondHome = MutableLiveData<SetState>()
    val secondHome: LiveData<SetState> = _secondHome

    /*---------------------------------------------------*/

    private val _secondAway = MutableLiveData<SetState>()
    val secondAway: LiveData<SetState> = _secondAway

    /*---------------------------------------------------*/

    private val _thirdHome = MutableLiveData<SetState>()
    val thirdHome: LiveData<SetState> = _thirdHome

    /*---------------------------------------------------*/

    private val _thirdAway = MutableLiveData<SetState>()
    val thirdAway: LiveData<SetState> = _thirdAway

    /*---------------------------------------------------*/

    private var isInputOk = true

    /*---------------------------------------------------*/

    fun inputResult(
        firstHome: String,
        firstAway: String,
        secondHome: String,
        secondAway: String,
        thirdHome: String,
        thirdAway: String,
        homePlayersText: Editable?,
        awayPlayersText: Editable?
    )
    {
        confirmInput(
            firstHome,
            firstAway,
            secondHome,
            secondAway,
            thirdHome,
            thirdAway,
            homePlayersText,
            awayPlayersText)
    }

    private fun confirmInput(
        firstHome: String,
        firstAway: String,
        secondHome: String,
        secondAway: String,
        thirdHome: String,
        thirdAway: String,
        homePlayersText: Editable?,
        awayPlayersText: Editable?
    )
    {
        isInputOk = true
        confirmGames(firstHome, _firstHome)
        confirmGames(firstAway, _firstAway)
        confirmGames(secondHome, _secondHome)
        confirmGames(secondAway, _secondAway)
        confirmGames(thirdHome, _thirdHome, optional = true)
        confirmGames(thirdAway, _thirdAway, optional = true)

        confirmSet(_firstHome, _firstAway)
    }

    private fun confirmSet(home: MutableLiveData<SetState>, away: MutableLiveData<SetState>)
    {
        if(home.value is ValidSet && away.value is ValidSet)
        {
            if(!SetState.validate((home.value as ValidSet).self, (away.value as ValidSet).self))
            {
                home.value = InvalidSet("Počet gemů v setu neodpovídá pravidlům.")
                away.value = InvalidSet("Počet gemů v setu neodpovídá pravidlům.")
            }
        }
    }

    private fun confirmGames(games: String, data: MutableLiveData<SetState>, optional: Boolean = false)
    {
        with(SetState.validate(games, optional)) {
            if(this is InvalidSet) isInputOk = false
            data.value = this
        }
    }
}
