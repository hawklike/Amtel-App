package cz.prague.cvut.fit.steuejan.amtelapp.fragments

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
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidEmail
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidPassword
import cz.prague.cvut.fit.steuejan.amtelapp.states.SignedUser
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.LoginFragmentVM

class LoginFragment : AbstractBaseFragment()
{
    private val viewModel by viewModels<LoginFragmentVM>()

    private lateinit var checkButton: FloatingActionButton
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    companion object
    {
        fun newInstance(): LoginFragment = LoginFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        checkButton = view.findViewById(R.id.login_checkButton)
        emailLayout = view.findViewById(R.id.login_email)
        passwordLayout = view.findViewById(R.id.login_password)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setToolbarTitle(AuthManager.getProfileDrawerOption(activity!!.applicationContext))
        setListeners()
        setObservers()
    }

    private fun setListeners()
    {
        checkButton.setOnClickListener {
            val email = emailLayout.editText!!.text.toString().trim()
            val password = passwordLayout.editText!!.text.toString().trim()

            deleteErrors()
            viewModel.loginUser(email, password)
        }
    }

    private fun setObservers()
    {
        confirmEmail()
        confirmPassword()
        getUser()
    }

    private fun confirmEmail()
    {
        viewModel.confirmEmail().observe(viewLifecycleOwner) { credentialState ->
            if(credentialState is InvalidEmail)
                emailLayout.error = getString(R.string.invalidEmail_error)
        }
    }

    private fun confirmPassword()
    {
        viewModel.confirmPassword().observe(viewLifecycleOwner) { credentialState ->
            if(credentialState is InvalidPassword)
                passwordLayout.error = getString(R.string.invalidPassword_error)
        }
    }

    private fun getUser()
    {
        //TODO: add loading bar [1]
        viewModel.getUser().observe(viewLifecycleOwner) { user ->
            val title: String
            val message: String?

            if(user is SignedUser)
            {
                title = getString(R.string.user_login_success_title)
                message = null
                Log.i(TAG, "getUser(): login was successful - current user: $user")
            }
            else
            {
                title = getString(R.string.user_login_failure_title)
                message = getString(R.string.user_login_failure_message)
                passwordLayout.editText!!.text.clear()
                Log.e(TAG, "getUser(): login not successful")
            }

            MaterialDialog(activity!!)
                .title(text = title).also {
                    if(message != null) it.message(text = message)
                }
                .show {
                    positiveButton(R.string.ok)
                    onDismiss {
                        if(user is SignedUser) mainActivityModel.setUser(SignedUser(user.self))
                    }
                }
        }
    }

    private val TAG = "LoginFragment"

    private fun deleteErrors()
    {
        emailLayout.error = null
        passwordLayout.error = null
    }
}