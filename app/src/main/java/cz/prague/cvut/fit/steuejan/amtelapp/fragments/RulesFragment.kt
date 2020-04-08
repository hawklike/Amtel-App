package cz.prague.cvut.fit.steuejan.amtelapp.fragments

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.lifecycle.observe
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.LeagueManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.RulesFragmentBinding
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment

class RulesFragment : AbstractMainActivityFragment()
{
    private var _binding: RulesFragmentBinding? = null
    private val binding get() = _binding!!

    companion object
    {
        fun newInstance(): RulesFragment = RulesFragment()
    }

    override fun getName(): String = "RulesFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = RulesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        binding.contactEmail.setOnClickListener(null)
        binding.contactPhone.setOnClickListener(null)
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setToolbarTitle(getString(R.string.rules))
        setLogoutIconVisibility(false)
        getHeadOfLeague()
        showRules()
        setListeners()
    }

    private fun setListeners()
    {
        binding.contactEmail.setOnClickListener {
            sendEmail()
        }

        binding.contactPhone.setOnClickListener {
            call()
        }
    }

    private fun sendEmail()
    {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            type = "message/rfc822"
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(LeagueManager.headOfLeague?.email ?: ""))
        }

        try { startActivity(Intent.createChooser(intent, "Poslat email" + "...")) }
        catch(ex: ActivityNotFoundException) { toast("Nemáte naistalovaný emailový klient.") }
    }

    private fun call()
    {
        LeagueManager.headOfLeague?.phone?.let {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$it")
            startActivity(intent)
        }
    }

    private fun getHeadOfLeague()
    {
        if(LeagueManager.headOfLeague == null)
        {
            mainActivityModel.initHeadOfLeague(5)
            mainActivityModel.headOfLeague.observe(viewLifecycleOwner) {
                LeagueManager.headOfLeague = it
                populateContactCard(it)
            }
        }
        else populateContactCard(LeagueManager.headOfLeague)
    }

    @SuppressLint("SetTextI18n")
    private fun populateContactCard(user: User?)
    {
        if(user == null) return
        binding.contactName.text = "${user.name} ${user.surname}"
        binding.contactEmail.text = user.email
        if(user.phone != null) binding.contactPhone.text = user.phone
        else binding.contactPhone.visibility = GONE
    }

    private fun showRules()
    {
        binding.rules.text = """
            Skupiny A, B, C, D hrají jednokolově každý s každým.
            
            Týmy ve skupině A, resp. B, resp. C, které skončí po dlouhodobé části na posledním místě, sestupují přímo do skupiny B, resp. C, resp. D.
            
            Týmy ve skupině A, resp. B, resp. C, které skončí po dlouhodobé části na předposledním místě, hrají baráž o udržení s druhými týmy ze skupiny B, resp. C, resp. D.
            
            Celkoví vítězové skupiny B, resp. C, resp. D, postupují přímo do vyšší skupiny.

            V každém utkání se odehrají 3 zápasy – dvě dvouhry a jedna čtyřhra. Vedoucí týmů se dohodnou na dvojicích pro dvouhry. Pokud nedojde k dohodě, rozhodne o soupeřích los. Není-li rozhodnuto jinak, hraje se čtyřhra jako závěrečný zápas.

            Zápasy se hrají dle pravidel tenisu, na dva vítězné sety. Za stavu 6:6 se v každém setu hraje tie break.
            Bodovaní utkání je následující: vítězství – 2 body, prohra – 1 bod, kontumace – 0 bodů.
            
            Při bodové shodě týmů v tabulce výsledků rozhoduje o pořadí vzájemné utkání (mezi dvěma týmy), nebo malá tabulka (tři a více týmů).

            Domácí tým dodá na první dva zápasy 8 nových míčů – Wilson US Open nebo Dunlop FORT TP.
        """.trimIndent()
    }
}