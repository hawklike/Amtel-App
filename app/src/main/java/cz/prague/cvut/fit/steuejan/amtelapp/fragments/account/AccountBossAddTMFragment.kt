package cz.prague.cvut.fit.steuejan.amtelapp.fragments.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.datetime.datePicker
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
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.AccountBossAddTMFragmentVM
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
    private lateinit var addUserButton: FloatingActionButton

    private lateinit var deadlineFromLayout: TextInputLayout
    private lateinit var deadlineToLayout: TextInputLayout

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
        addUserButton = view.findViewById(R.id.account_boss_add_tm_add)
        deadlineFromLayout = view.findViewById(R.id.account_boss_add_deadline_date_from)
        deadlineToLayout = view.findViewById(R.id.account_boss_add_deadline_date_to)
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
        deadlineFromLayout.editText?.setOnClickListener(null)
        deadlineToLayout.editText?.setOnClickListener(null)

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

    //TODO: refactor this
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
