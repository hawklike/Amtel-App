package cz.prague.cvut.fit.steuejan.amtelapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.authentication.AuthUtil
import cz.prague.cvut.fit.steuejan.amtelapp.models.LoginFragmentModel

class LoginFragment : AbstractBaseFragment()
{
    private val viewModel by viewModels<LoginFragmentModel>()

    companion object
    {
        fun newInstance(): LoginFragment = LoginFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setToolbarTitle(AuthUtil.getProfileDrawerOption(activity!!.applicationContext))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val checkButton = view.findViewById<FloatingActionButton>(R.id.login_checkButton)
        val emailLayout = view.findViewById<TextInputLayout>(R.id.login_email)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.login_password)

        checkButton.setOnClickListener {
            val email = emailLayout.editText!!.text.toString().trim()
            val password = passwordLayout.editText!!.text.toString().trim()

//            viewModel.foo.observe(viewLifecycleOwner) {
//                emailLayout.error = it
//            }

            viewModel.confirmLogin(email, password).observe(viewLifecycleOwner) {userState ->
                if(userState is LoginFragmentModel.UserState.InvalidUser)
                {
                    if(!userState.email) emailLayout.error = "Zadejte prosím platný email."
                    if(!userState.password) passwordLayout.error = "Vyplňte prosím heslo."
                }
            }

        }
    }
}