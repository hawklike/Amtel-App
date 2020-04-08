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
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.AccountPersonalFragmentVM

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

    private lateinit var fullNameLayout: TextInputLayout
    private lateinit var birthdateLayout: TextInputLayout
    private lateinit var phoneNumberLayout: TextInputLayout
    private lateinit var sexGroup: RadioGroup
    private lateinit var addPersonalInfo: FloatingActionButton

    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var changePassword: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.account_personal, container, false)
    }

    override fun getName(): String = "AccountPersonalFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        accountPersonalLayout = view.findViewById(R.id.account_personal)
        changePasswordLayout = view.findViewById(R.id.account_personal_change_password)
        personalInfoLayout = view.findViewById(R.id.account_personal_personal_information)

        fullNameLayout = view.findViewById(R.id.account_personal_personal_information_fullName)
        birthdateLayout = view.findViewById(R.id.account_personal_personal_information_birthdate)
        phoneNumberLayout = view.findViewById(R.id.account_personal_personal_information_phone)
        sexGroup = view.findViewById(R.id.account_personal_personal_information_sex)
        addPersonalInfo = view.findViewById(R.id.account_personal_personal_information_add_button)

        passwordLayout = view.findViewById(R.id.account_personal_password)
        confirmPasswordLayout = view.findViewById(R.id.account_personal_password_confirmation)
        changePassword = view.findViewById(R.id.account_personal_add_password_button)
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
        changePassword.setOnClickListener(null)
        addPersonalInfo.setOnClickListener(null)

        changePasswordLayout?.removeAllViews()
        personalInfoLayout?.removeAllViews()
        accountPersonalLayout?.removeAllViews()

        changePasswordLayout = null
        personalInfoLayout = null
        accountPersonalLayout = null
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
    }

    private fun getUser()
    {
        user = mainActivityModel.getUser().value ?: User()
//        mainActivityModel.getUser().observe(viewLifecycleOwner) { observedUser ->
//            user = observedUser?.copy() ?: user
//            Log.i("AccountPersonalFragment", "getUser(): user $user observed")
//        }
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
    }

    private fun isPersonalInformationSaved()
    {
        viewModel.isPersonalInfoChanged().observe(viewLifecycleOwner) { state ->
            progressDialog.dismiss()
            updateUser(state)
            val title = viewModel.createAfterPersonalInfoDialog(state).title

            MaterialDialog(activity!!)
                .title(text = title)
                .show {
                    positiveButton(R.string.ok)
                    onDismiss {
                    }
                }
        }
    }

    private fun updateUser(state: PersonalInfoState)
    {
        if(state is PersonalInfoSuccess)
        {
            user.name = state.name
            user.surname = state.surname
            user.birthdate = state.birthdate.toDate()
            user.phone = state.phoneNumber
            user.sex = state.sex.toBoolean()
            mainActivityModel.setUser(user)
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
                is ValidPassword -> displayDialog(password.self)
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

    private fun deletePersonalInfo()
    {
        fullNameLayout.error = null
        birthdateLayout.error = null
        phoneNumberLayout.error = null
    }

    private fun displayDialog(newPassword: String)
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

}
