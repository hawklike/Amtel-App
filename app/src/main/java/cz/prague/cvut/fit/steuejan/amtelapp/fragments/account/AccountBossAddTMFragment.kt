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
import cz.prague.cvut.fit.steuejan.amtelapp.activities.AbstractBaseActivity
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.AbstractBaseFragment
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
        setListeners()
        setObservers()
    }

    private fun setListeners()
    {
        addUserButton.setOnClickListener {
            val name = nameLayout.editText!!.text.toString().trim()
            val surname = surnameLayout.editText!!.text.toString().trim()
            val email = emailLayout.editText!!.text.toString().trim()

            deleteErrors()
//            displayDialog(name, surname, email)
            viewModel.createUser(activity!!.applicationContext, name, surname, email)
        }
    }

    private fun setObservers()
    {
        confirmName()
        confirmSurname()
        confirmEmail()
        isSuccessful()
    }

//    private fun displayDialog(name: String, surname: String, email: String)
//    {
//        MaterialDialog(activity!!)
//            .title("Opravdu chcete přidat tohoto vedoucího?")
//            .message(R.string.logout_message)
//            .show {
//                positiveButton(R.string.yes) {
//                    viewModel.createUser(activity!!.applicationContext, name, surname, email)
//                }
//                negativeButton(R.string.no)
//            }
//    }

    private fun confirmName()
    {
        viewModel.confirmName().observe(viewLifecycleOwner) { credentialState ->
            if(credentialState is AccountBossAddTMFragmentVM.NameState.InvalidName)
                nameLayout.error = getString(R.string.invalidName_error)
        }
    }

    private fun confirmSurname()
    {
        viewModel.confirmSurname().observe(viewLifecycleOwner) { credentialState ->
            if(credentialState is AccountBossAddTMFragmentVM.SurnameState.InvalidSurname)
                surnameLayout.error = getString(R.string.invalidSurname_error)
        }
    }

    private fun confirmEmail()
    {
        viewModel.confirmEmail().observe(viewLifecycleOwner) { credentialState ->
            if(credentialState is AccountBossAddTMFragmentVM.EmailState.InvalidEmail)
                emailLayout.error = getString(R.string.invalidEmail_error)
        }
    }

    private fun isSuccessful()
    {
        viewModel.isUserCreated().observe(viewLifecycleOwner) { isCreated ->
            val title: String
            val message: String?

            if(isCreated)
            {
                title = getString(R.string.user_registration_success_title)
                message = null
            }
            else
            {
                title = getString(R.string.user_registration_failure_title)
                message = getString(R.string.user_registration_failure_message)
            }

            MaterialDialog(activity!!)
                .title(text = title).also {
                    if(message != null) it.message(text = message)
                }
                .show {
                    positiveButton(R.string.ok)
                    onDismiss {
                        Log.i(AbstractBaseActivity.TAG, "registration")
                        deleteInput()
                    }
                }
        }
    }

    private fun deleteInput()
    {
        nameLayout.editText!!.text.clear()
        surnameLayout.editText!!.text.clear()
        emailLayout.editText!!.text.clear()
    }

    private fun deleteErrors()
    {
        nameLayout.error = null
        surnameLayout.error = null
        emailLayout.error = null
    }

}