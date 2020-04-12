package cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MatchManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.states.MatchState
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeam
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class PlayerInfoActivityVM : ViewModel()
{
    var mRounds: MutableList<Round>? = null
        private set
    var mGroupName: String? = null
        private set

    var roundNumber = 1

    var mUserId = ""
    var mUser: User? = null

    private var positiveGames = 0
    private var negativeGames = 0

    private var positiveSets = 0
    private var negativeSets = 0

    private var wins = 0
    private var losses = 0
    private var ties = 0

    /*---------------------------------------------------*/

    private val _rounds = MutableLiveData<List<Round>>()
    val rounds: LiveData<List<Round>> = _rounds

    /*---------------------------------------------------*/

    private val _player = MutableLiveData<User?>()
    val player: LiveData<User?> = _player

    /*---------------------------------------------------*/

    private val _group = MutableLiveData<String?>()
    val group: LiveData<String?> = _group

    /*---------------------------------------------------*/

    private val _match = SingleLiveEvent<MatchState>()
    val match: LiveData<MatchState> = _match

    /*---------------------------------------------------*/

    fun getPlayer()
    {
        if(mUser == null)
        {
            viewModelScope.launch {
                UserManager.findUser(mUserId)?.let {
                    mUser = it
                    mUserId = it.id!!
                    _player.value = it
                }
                    ?: let { _player.value = null }
            }
        }
        else
        {
            mUserId = mUser?.id!!
            _player.value = mUser
        }
    }

    fun getRounds()
    {
        mRounds = mutableListOf()
        viewModelScope.launch {
            UserManager.retrieveRounds(mUserId)?.let { playerRounds ->
                withContext(Default) {
                    with(playerRounds.rounds.values) {
                        forEach { rounds ->
                            val active = rounds.getActiveRounds()
                            setStatistics(active)
                            mRounds?.addAll(active)
                        }
                    }
                    mRounds = mRounds?.sortedByDescending { it.date }?.toMutableList()
                }
                _rounds.value = mRounds
            }
            ?: let { _rounds.value = emptyList() }
        }
    }

    fun getGroup()
    {
        viewModelScope.launch {
            with(TeamManager.findTeam(mUser?.teamId)) {
                if(this is ValidTeam)
                {
                    mGroupName = self.groupName
                    _group.value = self.groupName
                }
                else
                {
                    mGroupName = "-"
                    _group.value = null
                }
            }
        }
    }

    fun getMatch(matchId: String)
    {
        viewModelScope.launch {
            _match.value = MatchManager.findMatch(matchId)
        }
    }


    fun getSuccessRate(): Int
    {
        val rate = wins.toDouble() / (wins + losses + ties) * 100
        return if(!rate.isNaN()) rate.roundToInt() else 0
    }

    fun getNumberOfRounds(): Int
            = wins + losses + ties

    fun getChartData(): TripleEntries
    {
        val roundsEntries =
            if(getNumberOfRounds() != 0) listOf(PieEntry(wins.toFloat(), "Vyhrané zápasy"), PieEntry(losses.toFloat(), "Prohrané zápasy"), PieEntry(ties.toFloat(), "Remízy"))
            else listOf(PieEntry(1f, "Hráč ještě neodehrál žádné zápasy."))

        val setsEntries =
            if(positiveSets + negativeSets != 0) listOf(PieEntry(positiveSets.toFloat(), "Vyhrané sety"), PieEntry(negativeSets.toFloat(), "Prohrané sety"))
            else listOf(PieEntry(1f, "Hráč ještě neodehrál žádný set."))

        val gamesEntries =
            if(positiveGames + negativeGames != 0) listOf(PieEntry(positiveGames.toFloat(), "Vyhrané gemy"), PieEntry(negativeGames.toFloat(), "Prohrané gemy"))
            else listOf(PieEntry(1f, "Hráč ještě neodehrál žádný gem."))

        return Triple(roundsEntries, setsEntries, gamesEntries)
    }

    private fun setStatistics(rounds: List<Round>)
    {
        mUser?.let { user ->
            rounds.forEach { round ->
                val isHomePlayer = round.homePlayers.find { it.playerId == user.id }?.let { true } ?: false
                resolveRounds(isHomePlayer, round)
                resolveSets(isHomePlayer, round)
                resolveGames(isHomePlayer, round)
            }
        }
    }

    private fun resolveRounds(homePlayer: Boolean, round: Round)
    {
        if(homePlayer && round.homeWinner == true) wins++
        else if(homePlayer && round.homeWinner == false) losses++
        else if(!homePlayer && round.homeWinner == true) losses++
        else if(!homePlayer && round.homeWinner == false) wins++
        else ties++
    }

    private fun resolveSets(homePlayer: Boolean, round: Round)
    {
        if(homePlayer)
        {
            positiveSets += round.homeSets ?: 0
            negativeSets += round.awaySets ?: 0
        }
        else
        {
            positiveSets += round.awaySets ?: 0
            negativeSets += round.homeSets ?: 0
        }
    }

    private fun resolveGames(homePlayer: Boolean, round: Round)
    {
        if(homePlayer)
        {
            positiveGames += round.homeGems ?: 0
            negativeGames += round.awayGems ?: 0
        }
        else
        {
            positiveGames += round.awayGems ?: 0
            negativeGames += round.homeGems ?: 0
        }
    }
}
