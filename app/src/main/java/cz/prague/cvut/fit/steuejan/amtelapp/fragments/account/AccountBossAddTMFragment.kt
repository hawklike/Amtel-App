package cz.prague.cvut.fit.steuejan.amtelapp.fragments.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.EmailSender
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.shrinkWhitespaces
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidCredentials
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidCredentials
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidRegistration
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments.AccountBossAddTMFragmentVM
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.*

class AccountBossAddTMFragment : AbstractMainActivityFragment()
{
    companion object
    {
        fun newInstance(): AccountBossAddTMFragment = AccountBossAddTMFragment()
    }

    private val viewModel by viewModels<AccountBossAddTMFragmentVM>()

    private var addTeamManagerLayout: RelativeLayout? = null
    private var chooseDeadlineLayout: RelativeLayout? = null

    private lateinit var nameLayout: TextInputLayout
    private lateinit var surnameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var chooseTeam: CheckBox
    private lateinit var chosenTeam: TextView
    private lateinit var addUserButton: FloatingActionButton

    private lateinit var deadlineFromLayout: TextInputLayout
    private lateinit var deadlineToLayout: TextInputLayout

    private lateinit var deleteDeadline: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.account_boss_add_tm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        nameLayout = view.findViewById(R.id.account_boss_add_tm_name)
        surnameLayout = view.findViewById(R.id.account_boss_add_tm_surname)
        emailLayout = view.findViewById(R.id.account_boss_add_tm_email)
        chooseTeam = view.findViewById(R.id.account_boss_add_tm_choose_team)
        chosenTeam = view.findViewById(R.id.account_boss_add_tm_chosen_team)
        addUserButton = view.findViewById(R.id.account_boss_add_tm_add)
        deadlineFromLayout = view.findViewById(R.id.account_boss_add_deadline_date_from)
        deadlineToLayout = view.findViewById(R.id.account_boss_add_deadline_date_to)
        deleteDeadline = view.findViewById(R.id.account_boss_deadline_delete)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setObservers()
        setListeners()
    }

    override fun getName(): String = "AccountBossAddTMFragment"

    override fun onDestroyView()
    {
        super.onDestroyView()
        addUserButton.setOnClickListener(null)
        chooseTeam.setOnCheckedChangeListener(null)
        deadlineFromLayout.editText?.setOnClickListener(null)
        deadlineToLayout.editText?.setOnClickListener(null)
        deleteDeadline.setOnClickListener(null)

        addTeamManagerLayout?.removeAllViews()
        chooseDeadlineLayout?.removeAllViews()

        addTeamManagerLayout = null
        chooseDeadlineLayout = null
    }

    private fun setListeners()
    {
        addUserButton.setOnClickListener {
            if(!EmailSender.hasPassword)
            {
                toast(getString(R.string.server_error_email_noTmAdded))
                return@setOnClickListener
            }

            val name = nameLayout.editText?.text.toString().trim().shrinkWhitespaces()
            val surname = surnameLayout.editText?.text.toString().trim().shrinkWhitespaces()
            val email = emailLayout.editText?.text.toString().trim()

            deleteErrors()
            viewModel.confirmCredentials(name, surname, email)
        }

        deadlineFromLayout.editText?.setOnClickListener {
           showDeadlineDialog(deadlineFromLayout, true)
        }

        deadlineToLayout.editText?.setOnClickListener {
            showDeadlineDialog(deadlineToLayout, false)
        }

        deleteDeadline.setOnClickListener {
            MaterialDialog(activity!!).show {
                title(text = "Vymazat deadline soupisky?")
                negativeButton()
                positiveButton(text = "Smazat") {
                    progressDialog.show()
                    viewModel.deleteDeadline()
                    deadlineFromLayout.editText?.text?.clear()
                    deadlineToLayout.editText?.text?.clear()
                }
            }
        }

        chooseTeam.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked)
            {
                MaterialDialog(activity!!).show {
                    title(text = "Přidat vedoucího do již existujícího týmu?")
                    message(text = "Stávající vedoucí týmu bude nahrazen novým vedoucím.")
                    positiveButton(text = "Vybrat tým") {
                        progressDialog.show()
                        showTeams()
                    }
                    negativeButton {
                        chooseTeam.isChecked = false
                    }
                }
            }
            else
            {
                viewModel.chosenTeam = null
                chosenTeam.visibility = GONE
                chosenTeam.text = null
            }
        }
    }

    private fun showTeams()
    {
        viewModel.retrieveAllTeams()
        viewModel.teams.observe(viewLifecycleOwner) { teams ->
            progressDialog.dismiss()
            MaterialDialog(activity!!).show {
                title(text = "Vybrat tým")

                val teamNames = teams.map { it.name }
                listItemsSingleChoice(items = teamNames) { _, index, _ ->
                    viewModel.chosenTeam = teams[index]
                }
                onDismiss {
                    viewModel.chosenTeam?.let {
                        toast("Byl vybrán tým ${it.name}.")
                        chosenTeam.visibility = VISIBLE
                        chosenTeam.text = it.name
                    }
                    ?: toast("Nebyl vybrán žádný tým.")
                }
            }
        }
    }

    private fun showDeadlineDialog(textInputLayout: TextInputLayout, from: Boolean)
    {
        var deadline = Date()
        MaterialDialog(activity!!).show {
            val savedDate = textInputLayout.editText?.text?.let {
                viewModel.setDialogDeadline(it)
            }

            datePicker(currentDate = savedDate) { _, date ->
                val dateText = date.toMyString()
                deadline = date.time
                textInputLayout.editText?.setText(dateText)
            }
            positiveButton(text = "Uložit") {
                viewModel.setDeadline(deadline, from)
                progressDialog.show()
            }
        }
    }

    private fun setObservers()
    {
        getDeadline()
        registerUser()
        isRegistrationSuccessful()
        isDeadlineAdded()
        isDeadlineDeleted()
    }

    private fun isDeadlineDeleted()
    {
        viewModel.isDeadlineDeleted.observe(viewLifecycleOwner) { deleted ->
            progressDialog.dismiss()
            if(deleted) toast("Úspěšně smazáno.")
            else toast("Smazání se nepodařilo.")
        }
    }

    private fun getDeadline()
    {
        viewModel.getDeadline()
        viewModel.deadline.observe(viewLifecycleOwner) {
            deadlineFromLayout.editText?.setText(it.first)
            deadlineToLayout.editText?.setText(it.second)
        }
    }

    private fun isDeadlineAdded()
    {
        viewModel.isDeadlineAdded.observe(viewLifecycleOwner) { success ->
            progressDialog.dismiss()
            if(success) toast("Deadline byl úspěšně uložen.")
            else toast("Deadline se nepodařilo uložit.")
        }
    }

    private fun registerUser()
    {
        viewModel.credentials.observe(viewLifecycleOwner) { credentials ->
            if(credentials is ValidCredentials) displayDialog(credentials)
            if(credentials is InvalidCredentials)
            {
                if(!credentials.name) nameLayout.error = getString(R.string.invalidName_error)
                if(!credentials.surname) surnameLayout.error = getString(R.string.invalidSurname_error)
                if(!credentials.email)  emailLayout.error = getString(R.string.invalidEmail_error)
            }
        }
    }

    private fun displayDialog(credentials: ValidCredentials)
    {
        MaterialDialog(activity!!)
            .title(R.string.user_registration_confirmation_title)
            .message(text = "${credentials.name} ${credentials.surname}\n${credentials.email}")
            .show {
                positiveButton(text = "Přidat") {
                    viewModel.createUser(credentials)
                    progressDialog.show()
                }
                negativeButton()
            }
    }

    private fun isRegistrationSuccessful()
    {
        viewModel.registration.observe(viewLifecycleOwner) { registration ->
            progressDialog.dismiss()

            val title: String
            val message: String

            if(registration is ValidRegistration)
            {
                title = getString(R.string.user_registration_success_title)
                message = getString(R.string.user_registration_success_message)
            }
            else
            {
                title = getString(R.string.user_registration_failure_title)
                message = getString(R.string.user_registration_failure_message)
            }

            MaterialDialog(activity!!)
                .title(text = title).also {
                    it.message(text = message)
                }
                .show {
                    positiveButton(R.string.ok)
                    onDismiss {
                        Log.i(TAG, "registration completed")
                        deleteInput()
                    }
                }
        }
    }

    private fun deleteInput()
    {
        nameLayout.editText?.text?.clear()
        surnameLayout.editText?.text?.clear()
        emailLayout.editText?.text?.clear()
    }

    private fun deleteErrors()
    {
        nameLayout.error = null
        surnameLayout.error = null
        emailLayout.error = null
    }

}
