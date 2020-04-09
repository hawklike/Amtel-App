package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Player
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.PlayerInfoBinding
import java.util.*

class PlayerInfoActivity : AbstractBaseActivity()
{
    private lateinit var binding: PlayerInfoBinding

    private var player = Player()

    companion object
    {
        const val PLAYER = "player"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        binding = PlayerInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        super.onCreate(savedInstanceState)
        getPlayer()
        setToolbarTitle("${player.name} ${player.surname}")
        setArrowBack()
        setTeamName()
        setAge()
    }

    private fun setTeamName()
    {
        binding.team.text = player.teamName ?: "Bez týmu"
    }

    private fun setAge()
    {
        binding.age.text = DateUtil.getAge(player.birthdate ?: Date()).toString()
    }

    private fun getPlayer()
    {
        intent.extras?.let { bundle ->
            player = bundle.getParcelable(PLAYER) ?: Player()
        }
    }
}