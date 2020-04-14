package cz.prague.cvut.fit.steuejan.amtelapp.fragments.account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.datetime.datePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.removeWhitespaces
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.shrinkWhitespaces
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toDate
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Sex
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toSex
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments.AccountPersonalFragmentVM

class AccountPersonalFragment : AbstractMainActivityFragment()
{
    companion object
    {
        fun newInstance(): AccountPersonalFragment = AccountPersonalFragment()
    }

    private val viewModel by viewModels<AccountPersonalFragmentVM>()

    private lateinit var user: User

    private var accountPersonalLayout: RelativeLayout? = null
    private var changePasswordLayout: RelativeLayout? = null
    private var personalInfoLayout: RelativeLayout? = null
    private var changeEmailLayout: RelativeLayout? = null

    private lateinit var fullNameLayout: TextInputLayout
    private lateinit var birthdateLayout: TextInputLayout
    private lateinit var phoneNumberLayout: TextInputLayout
    private lateinit var sexGroup: RadioGroup
    private lateinit var addPersonalInfo: FloatingActionButton

    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var changePassword: FloatingActionButton

    private lateinit var emailLayout: TextInputLayout
    private lateinit var changeEmail: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.account_personal, container, false)
    }

    override fun getName(): String = "AccountPersonalFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        personalInfoLayout = view.findViewById(R.id.account_personal_personal_information)
        changePasswordLayout = view.findViewById(R.id.account_personal_change_password)
        accountPersonalLayout = view.findViewById(R.id.account_personal)

        addPersonalInfo = view.findViewById(R.id.account_personal_personal_information_add_button)
        birthdateLayout = view.findViewById(R.id.account_personal_personal_information_birthdate)
        phoneNumberLayout = view.findViewById(R.id.account_personal_personal_information_phone)
        fullNameLayout = view.findViewById(R.id.account_personal_personal_information_fullName)
        sexGroup = view.findViewById(R.id.account_personal_personal_information_sex)

        confirmPasswordLayout = view.findViewById(R.id.account_personal_password_confirmation)
        changePassword = view.findViewById(R.id.account_personal_add_password_button)
        passwordLayout = view.findViewById(R.id.account_personal_password)

        changeEmail = view.findViewById(R.id.account_personal_change_email_button)
        changeEmailLayout = view.findViewById(R.id.account_personal_change_email)
        emailLayout = view.findViewById(R.id.account_personal_email)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setObservers()
        setListeners()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        sexGroup.setOnCheckedChangeListener(null)
        addPersonalInfo.setOnClickListener(null)
        changePassword.setOnClickListener(null)
        changeEmail.setOnClickListener(null)

        accountPersonalLayout?.removeAllViews()
        changePasswordLayout?.removeAllViews()
        personalInfoLayout?.removeAllViews()
        changeEmailLayout?.removeAllViews()

        accountPersonalLayout = null
        changePasswordLayout = null
        personalInfoLayout = null
        changeEmailLayout = null
    }

    private fun setObservers()
    {
        getUser()
        updatePersonalInfo()
        confirmName()
        confirmPassword()
        confirmBirthdate()
        confirmPhoneNumber()
        isPasswordChanged()
        isPersonalInformationSaved()
        isEmailChanged()
    }

    private fun getUser()
    {
        user = mainActivityModel.getUser().value ?: User()
    }

    @SuppressLint("SetTextI18n")
    private fun updatePersonalInfo()
    {
        fullNameLayout.editText?.setText("${user.name} ${user.surname}")

        user.birthdate?.let {
            birthdateLayout.editText?.setText(it.toMyString())
        }

        user.phone?.let {
            phoneNumberLayout.editText?.setText(it)
        }

        val rb: RadioButton = if(user.sex.toSex() == Sex.MAN) view!!.findViewById(R.id.account_personal_personal_information_sex_man)
        else view!!.findViewById(R.id.account_personal_personal_information_sex_woman)
        rb.isChecked = true
    }

    private fun setListeners()
    {
        var sex = Sex.MAN
        sexGroup.setOnCheckedChangeListener { _, checkedId ->
            val rb = view!!.findViewById<RadioButton>(checkedId)
            sex = if(rb.id == R.id.account_personal_personal_information_sex_man) Sex.MAN else Sex.WOMAN
        }

        changePassword.setOnClickListener {
            val password = passwordLayout.editText?.text.toString()
            val confirmation = confirmPasswordLayout.editText?.text.toString()
            passwordLayout.error = null
            confirmPasswordLayout.error = null
            viewModel.confirmPassword(password, confirmation)
        }

        addPersonalInfo.setOnClickListener {
            progressDialog.show()
            val fullName = fullNameLayout.editText?.text.toString().trim().shrinkWhitespaces()
            val birthdate = birthdateLayout.editText?.text.toString().trim()
            val phoneNumber = phoneNumberLayout.editText?.text.toString().removeWhitespaces()
            deletePersonalInfo()
            viewModel.savePersonalInfo(user, fullName, birthdate, phoneNumber, sex)
        }

        birthdateLayout.editText?.setOnClickListener {
            MaterialDialog(activity!!).show {
                val savedDate = birthdateLayout.editText?.text?.let {
                    viewModel.setDialogBirthdate(it)
                }

                datePicker(currentDate = savedDate) { _, date ->
                    val dateText = date.toMyString()
                    birthdateLayout.editText?.setText(dateText)
                }
            }
        }

        changeEmail.setOnClickListener {
            val email = emailLayout.editText?.text.toString().trim()
            emailLayout.error = null
            if(viewModel.confirmEmail(email)) displayChangeEmailDialog(email)
            else emailLayout.error = getString(R.string.email_failure_message)
        }
    }

    private fun isPersonalInformationSaved()
    {
        viewModel.isPersonalInfoChanged().observe(viewLifecycleOwner) { state ->
            progressDialog.dismiss()
            update(state)
            val title = viewModel.createAfterPersonalInfoDialog(state).title

            MaterialDialog(activity!!)
                .title(text = title)
                .show {
                    positiveButton(R.string.ok)
                    onDismiss {}
                }
        }
    }

    private fun update(state: PersonalInfoState)
    {
        if(state is PersonalInfoSuccess)
        {
            updateTeam(user.teamId)
            user.name = state.name
            user.surname = state.surname
            user.birthdate = state.birthdate.toDate()
            user.phone = state.phoneNumber
            user.sex = state.sex.toBoolean()
            mainActivityModel.setUser(user)
        }
    }

    private fun updateTeam(teamId: String?)
    {
        viewModel.updateTeam(teamId)
        viewModel.isTeamUpdated().observe(viewLifecycleOwner) { team ->
            mainActivityModel.setTeam(ValidTeam(team))
        }
    }

    private fun confirmPassword()
    {
        viewModel.confirmPassword().observe(viewLifecycleOwner) { password ->
            when(password)
            {
                is InvalidPassword ->
                {
                    if(password.errorMessage == getString(R.string.password_invalid_no_match))
                    {
                        confirmPasswordLayout.error = password.errorMessage
                        confirmPasswordLayout.editText?.text?.clear()
                    }
                    passwordLayout.error = password.errorMessage
                    passwordLayout.editText?.text?.clear()
                }
                is ValidPassword -> displayChangePasswordDialog(password.self)
            }
        }
    }

    private fun confirmName()
    {
        viewModel.confirmName().observe(viewLifecycleOwner) { nameState ->
            if(nameState is InvalidName)
                fullNameLayout.error = nameState.errorMessage
            progressDialog.dismiss()
        }
    }

    private fun confirmBirthdate()
    {
        viewModel.confirmBirthdate().observe(viewLifecycleOwner) { birthdateState ->
            if(birthdateState is InvalidBirthdate)
                birthdateLayout.error = birthdateState.errorMessage
            progressDialog.dismiss()
        }
    }

    private fun confirmPhoneNumber()
    {
        viewModel.confirmPhoneNumber().observe(viewLifecycleOwner) { phoneUmberState ->
            if(phoneUmberState is InvalidPhoneNumber)
                phoneNumberLayout.error = phoneUmberState.errorMessage
            progressDialog.dismiss()
        }
    }

    private fun isPasswordChanged()
    {
        viewModel.isPasswordChanged().observe(viewLifecycleOwner) { success ->
            progressDialog.dismiss()
            val dialog = viewModel.createAfterPasswordChangeDialog(success)

            val title = dialog.first
            val message = dialog.second

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

    private fun isEmailChanged()
    {
        viewModel.isEmailChanged().observe(viewLifecycleOwner) { email ->
            if(email is ValidEmail)
            {
                viewModel.updateUserEmail(user, email.self)
                viewModel.isUserUpdated().observe(viewLifecycleOwner) { updated ->
                    if(updated)
                    {
                        progressDialog.dismiss()
                        updateTeam(user.teamId)
                        showDialog("Email byl úspěšně změněn", "Nyní se přihlásíte pomocí nového emailu.")
                    }
                }
            }
            else
            {
                progressDialog.dismiss()
                showDialog("Email se nepodařilo změnit", (email as InvalidEmail).errorMessage)
            }
        }
    }

    private fun showDialog(title: String, message: String)
    {
        MaterialDialog(activity!!).show {
            title(text = title)
            message(text = message)
            positiveButton()
        }
    }

    private fun deletePersonalInfo()
    {
        fullNameLayout.error = null
        birthdateLayout.error = null
        phoneNumberLayout.error = null
    }

    private fun displayChangePasswordDialog(newPassword: String)
    {
        MaterialDialog(activity!!)
            .title(R.string.password_change_confirmation_title)
            .show {
                positiveButton(text = "Změnit") {
                    viewModel.addNewPassword(newPassword)
                    progressDialog.show()
                }
                negativeButton {
                    passwordLayout.editText?.text?.clear()
                    confirmPasswordLayout.editText?.text?.clear()
                }
            }
    }

    private fun displayChangeEmailDialog(newEmail: String)
    {
        MaterialDialog(activity!!)
            .title(text = "Opravdu chcete změnit email?")
            .show {
                positiveButton(text = "Změnit") {
                    viewModel.changeEmail(newEmail)
                    progressDialog.show()
                }
                negativeButton {
                    emailLayout.editText?.text?.clear()
                }
            }
    }

}
