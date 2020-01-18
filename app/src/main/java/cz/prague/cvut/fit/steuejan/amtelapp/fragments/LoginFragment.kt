package cz.prague.cvut.fit.steuejan.amtelapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.AuthManager
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
        viewModel.confirmEmail().observe(viewLifecycleOwner) { credentialState ->
            if(credentialState is LoginFragmentVM.EmailState.InvalidEmail)
                emailLayout.error = "Zadejte prosím validní email."
        }

        viewModel.confirmPassword().observe(viewLifecycleOwner) { credentialState ->
            if(credentialState is LoginFragmentVM.PasswordState.InvalidPassword)
                passwordLayout.error = "Vyplňte prosím heslo."
        }

        //TODO: replace login with an actual account screen after successful login [3]
        //TODO: add loading bar [1]
        //TODO: add confirmation [2]
        viewModel.getUser().observe(viewLifecycleOwner) { user ->
            deleteText()
        }
    }

    private fun deleteErrors()
    {
        emailLayout.error = null
        passwordLayout.error = null
    }

    private fun deleteText()
    {
        emailLayout.editText!!.text.clear()
        passwordLayout.editText!!.text.clear()
    }
}