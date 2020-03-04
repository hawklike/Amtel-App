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
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidEmail
import cz.prague.cvut.fit.steuejan.amtelapp.states.InvalidPassword
import cz.prague.cvut.fit.steuejan.amtelapp.states.NoUser
import cz.prague.cvut.fit.steuejan.amtelapp.states.SignedUser
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.LoginFragmentVM

class LoginFragment : AbstractMainActivityFragment()
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

    override fun getName(): String = "LoginFragment"

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
        setToolbarTitle(AuthManager.profileDrawerOption)
        setListeners()
        setObservers()
    }

    private fun setListeners()
    {
        checkButton.setOnClickListener {
            val email = emailLayout.editText!!.text.toString().trim()
            val password = passwordLayout.editText!!.text.toString().trim()

            deleteErrors()
            loading(true)
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
                emailLayout.error = credentialState.errorMessage
        }
    }

    private fun confirmPassword()
    {
        viewModel.confirmPassword().observe(viewLifecycleOwner) { credentialState ->
            if(credentialState is InvalidPassword)
                passwordLayout.error = credentialState.errorMessage
        }
    }

    private fun getUser()
    {
        viewModel.getUser().observe(viewLifecycleOwner) { user ->
            val dialog = viewModel.createAfterDialog(user)
            val title = dialog.title
            val message = dialog.message

            if(user is NoUser) passwordLayout.editText?.text?.clear()

            MaterialDialog(activity!!)
                .title(text = title).also {
                    if(message != null) it.message(text = message)
                }
                .show {
                    positiveButton(R.string.ok)
                    onDismiss {
                        loading(false)
                        if(user is SignedUser)
                        {
                            mainActivityModel.isUserLoggedIn(SignedUser(user.self, user.firstSign))
                            mainActivityModel.setUser(user.self)
                        }
                    }
                }
        }
    }

    private fun deleteErrors()
    {
        emailLayout.error = null
        passwordLayout.error = null
    }

    private fun loading(on: Boolean)
    {
        setProgressBar(on)
        when(on)
        {
            true -> {
                emailLayout.visibility = View.GONE
                passwordLayout.visibility = View.GONE
                checkButton.visibility = View.GONE
            }
            false -> {
                emailLayout.visibility = View.VISIBLE
                passwordLayout.visibility = View.VISIBLE
                checkButton.visibility = View.VISIBLE
            }
        }
    }
}