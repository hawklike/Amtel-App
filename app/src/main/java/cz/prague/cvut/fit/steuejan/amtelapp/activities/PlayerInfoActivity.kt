package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.normal.ShowPlayerRoundsAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Player
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Round
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.PlayerInfoBinding
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.PlayerInfoActivityVM
import java.util.*

class PlayerInfoActivity : AbstractBaseActivity()
{
    private lateinit var binding: PlayerInfoBinding

    private var adapter: ShowPlayerRoundsAdapter? = null

    private val viewModel by viewModels<PlayerInfoActivityVM>()

    private var player = Player()

    companion object
    {
        const val PLAYER = "player"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        binding = PlayerInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        getPlayer()
        setToolbarTitle("${player.name} ${player.surname}")
        setArrowBack()
        setTeamName()
        setAge()
        getRounds()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        binding.roundsRecyclerView.adapter = null
        adapter = null

    }

    private fun getPlayer()
    {
        intent.extras?.let { bundle ->
            player = bundle.getParcelable(PLAYER) ?: Player()
        }
    }

    private fun setTeamName()
    {
        binding.team.text = player.teamName ?: "Bez týmu"
    }

    private fun setAge()
    {
        binding.age.text = DateUtil.getAge(player.birthdate ?: Date()).toString()
    }

    private fun getRounds()
    {
        if(viewModel.mRounds == null) viewModel.getRounds(player)
        viewModel.rounds.observe(this) {
            setupRecycler(it)
        }
    }

    private fun setupRecycler(rounds: List<Round>)
    {
        binding.roundsRecyclerView.setHasFixedSize(true)
        binding.roundsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        adapter = ShowPlayerRoundsAdapter(rounds, player)
        binding.roundsRecyclerView.adapter = adapter
    }

}