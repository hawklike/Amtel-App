package cz.prague.cvut.fit.steuejan.amtelapp.fragments.account

import android.os.Bundle
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
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.AbstractBaseFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidPassword
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidPassword
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.AccountPersonalVM

class AccountPersonalFragment : AbstractBaseFragment()
{
    companion object
    {
        const val DATA = "user"

        fun newInstance(user: User): AccountPersonalFragment
        {
            val fragment = AccountPersonalFragment()
            val bundle = Bundle()
            bundle.putParcelable(DATA, user)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val viewModel by viewModels<AccountPersonalVM>()

    private lateinit var passwordLayout: TextInputLayout
    private lateinit var addNewPassword: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.account_personal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        passwordLayout = view.findViewById(R.id.account_personal_password)
        addNewPassword = view.findViewById(R.id.account_personal_add_password_button)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setObservers()
        setListeners()
    }

    private fun setObservers()
    {
        confirmPassword()
        isPasswordChanged()
    }

    private fun setListeners()
    {
        addNewPassword.setOnClickListener {
            val password = passwordLayout.editText!!.text.toString().trim()
            passwordLayout.error = null
            viewModel.confirmPassword(password)
        }
    }

    private fun confirmPassword()
    {
        viewModel.confirmPassword().observe(viewLifecycleOwner) { password ->
            when(password)
            {
                is InvalidPassword ->
                {
                    passwordLayout.error = password.errorMessage
                    passwordLayout.editText?.text?.clear()
                }
                is ValidPassword -> displayDialog(password.self)
            }
        }
    }

    private fun isPasswordChanged()
    {
        viewModel.isPasswordChanged().observe(viewLifecycleOwner) { success ->
            val title: String
            val message: String?

            if(success)
            {
                title = getString(R.string.password_change_success_title)
                message = null
            }
            else
            {
                title = getString(R.string.password_change_failure_title)
                message = getString(R.string.password_change_failure_message)
            }

            MaterialDialog(activity!!)
                .title(text = title).also {
                    if(message != null) it.message(text = message)
                }
                .show {
                    positiveButton(R.string.ok)
                    onDismiss {
                        passwordLayout.editText?.text?.clear()
                    }
                }
        }
    }


    private fun displayDialog(newPassword: String)
    {
        MaterialDialog(activity!!)
            .title(R.string.password_change_confirmation_title)
            .message(text = "Nové heslo: $newPassword")
            .show {
                positiveButton(R.string.yes) {
                    viewModel.addNewPassword(newPassword)
                }
                negativeButton(R.string.no) {
                    passwordLayout.editText?.text?.clear()
                }
            }
    }

}