package cz.prague.cvut.fit.steuejan.amtelapp.fragments.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.UserManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.EmailSender
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.AbstractBaseFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidCredentials
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidCredentials
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidRegistration
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.AccountBossAddTMFragmentVM

class AccountBossAddTMFragment : AbstractBaseFragment()
{
    companion object
    {
        fun newInstance(): AccountBossAddTMFragment = AccountBossAddTMFragment()
    }

    private val viewModel by viewModels<AccountBossAddTMFragmentVM>()

    private lateinit var nameLayout: TextInputLayout
    private lateinit var surnameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var addUserButton: FloatingActionButton
    private lateinit var dialog: MaterialDialog

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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setObservers()
        setListeners()
    }

    override fun onPause()
    {
        super.onPause()
        if(::dialog.isInitialized) dialog.dismiss()
    }

    private fun setListeners()
    {
        addUserButton.setOnClickListener {
            val name = nameLayout.editText!!.text.toString().trim()
            val surname = surnameLayout.editText!!.text.toString().trim()
            val email = emailLayout.editText!!.text.toString().trim()

            deleteErrors()
            viewModel.confirmCredentials(name, surname, email)
        }
    }

    private fun setObservers()
    {
        registerUser()
        isRegistrationSuccessful()
    }

    private fun registerUser()
    {
        viewModel.isCredentialsValid().observe(viewLifecycleOwner) { credentialsState ->
            if(credentialsState is ValidCredentials)
                displayDialog(credentialsState)
            if(credentialsState is InvalidCredentials)
            {
                if(!credentialsState.name) nameLayout.error = getString(R.string.invalidName_error)
                if(!credentialsState.surname) surnameLayout.error = getString(R.string.invalidSurname_error)
                if(!credentialsState.email)  emailLayout.error = getString(R.string.invalidEmail_error)
            }
        }
    }

    private fun displayDialog(credentials: ValidCredentials)
    {
        dialog = MaterialDialog(activity!!)
            .title(R.string.user_registration_confirmation_title)
            .message(text = "${credentials.name} ${credentials.surname}\n${credentials.email}")
            .show {
                positiveButton(R.string.yes) {
                    viewModel.createUser(
                        activity!!,
                        credentials
                    )
                }
                negativeButton(R.string.no)
            }
    }

    private fun isRegistrationSuccessful()
    {
        viewModel.isUserCreated().observe(viewLifecycleOwner) { registration ->
            val title: String
            val message: String

            if(registration is ValidRegistration)
            {
                title = getString(R.string.user_registration_success_title)
                message = getString(R.string.user_registration_success_message)

                val (name, surname, email) = registration.credentials
                UserManager.addUser(registration.uid, name, surname, email, UserRole.TEAM_MANAGER)
                EmailSender.sendVerificationEmail(activity!!, email, registration.password)
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