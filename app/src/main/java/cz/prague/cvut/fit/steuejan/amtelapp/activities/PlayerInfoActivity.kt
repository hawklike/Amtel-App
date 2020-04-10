package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.normal.ShowPlayerRoundsAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Player
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.toPlayer
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.PlayerInfoBinding
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidMatch
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.PlayerInfoActivityVM
import java.util.*

class PlayerInfoActivity : AbstractBaseActivity()
{
    private lateinit var binding: PlayerInfoBinding

    private var adapter: ShowPlayerRoundsAdapter? = null

    private val viewModel by viewModels<PlayerInfoActivityVM>()

    private val progressDialog by lazy {
        MaterialDialog(this)
            .customView(R.layout.progress_layout)
    }

    companion object
    {
        const val PLAYER_ID = "playerId"
        const val PLAYER = "player"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        binding = PlayerInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        setToolbarTitle("Načítám hráče...")
        setArrowBack()
        getPlayer()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        adapter?.onClick = null
        binding.roundsRecyclerView.adapter = null
        adapter = null
    }

    private fun getPlayer()
    {
        intent.extras?.let { bundle ->
            viewModel.mUserId = bundle.getString(PLAYER_ID, "Není mucus jako mucus.")
            viewModel.mUser = bundle.getParcelable(PLAYER)
        }
        viewModel.getPlayer()
        viewModel.player.observe(this) { player ->
            initAll(player)
        }
    }

    private fun initAll(player: User?)
    {
        if(!isPlayerExisting(player)) return
        setToolbarTitle("${player?.name} ${player?.surname}")
        setTeamName()
        setAge()
        getRounds()
        getGroup()
    }


    private fun isPlayerExisting(player: User?): Boolean
    {
        if(player == null)
        {
            MaterialDialog(this).show {
                title(text = "Hráč byl v minulosti smazán")
                positiveButton(text = "To je mi líto") {
                    this@PlayerInfoActivity.onBackPressed()
                }
            }
            return false
        }
        return true
    }

    private fun setTeamName()
    {
        binding.team.text = viewModel.mUser?.teamName ?: "-"
    }

    private fun setAge()
    {
        binding.age.text = DateUtil.getAge(viewModel.mUser?.birthdate ?: Date()).toString()
    }

    private fun getRounds()
    {
        if(viewModel.mRounds == null) viewModel.getRounds()
        viewModel.rounds.observe(this) {
            setupRecycler(it)
        }
    }

    private fun getGroup()
    {
        if(viewModel.mGroupName == null) viewModel.getGroup()
        else binding.group.text = viewModel.mGroupName

        viewModel.group.observe(this) {
            binding.group.text = it ?: "-"
        }
    }

    private fun setupRecycler(rounds: List<Round>)
    {
        binding.roundsRecyclerView.setHasFixedSize(true)
        binding.roundsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = ShowPlayerRoundsAdapter(rounds, viewModel.mUser?.toPlayer() ?: Player())

        adapter?.onClick = { round ->
            viewModel.roundNumber = round.round
            progressDialog.show()
            viewModel.getMatch(round.matchId)
            viewModel.match.observe(this) { match ->
                progressDialog.dismiss()
                if(match is ValidMatch)
                {
                    val intent = Intent(this, MatchViewPagerActivity::class.java).apply {
                        putExtra(MatchViewPagerActivity.ROUND, viewModel.roundNumber - 1)
                        putExtra(MatchViewPagerActivity.MATCH, match.self)
                        putExtra(MatchViewPagerActivity.TITLE, getString(R.string.match_result))
                    }
                    startActivity(intent)
                }
            }
        }

        binding.roundsRecyclerView.adapter = adapter
    }

}