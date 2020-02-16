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
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toDate
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Sex
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toSex
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.AbstractBaseFragment
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.AccountPersonalFragmentVM

class AccountPersonalFragment : AbstractBaseFragment()
{
    companion object
    {
        fun newInstance(): AccountPersonalFragment = AccountPersonalFragment()
    }

    private val viewModel by viewModels<AccountPersonalFragmentVM>()

    private lateinit var user: User

    private var changePasswordLayout: RelativeLayout? = null
    private var personalInfoLayout: RelativeLayout? = null

    private lateinit var passwordLayout: TextInputLayout
    private lateinit var addNewPassword: FloatingActionButton

    private lateinit var fullNameLayout: TextInputLayout
    private lateinit var birthdateLayout: TextInputLayout
    private lateinit var phoneNumberLayout: TextInputLayout
    private lateinit var sexGroup: RadioGroup
    private lateinit var addPersonalInfo: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.account_personal, container, false)
    }

    override fun getName(): String = "AccountPersonalFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        changePasswordLayout = view.findViewById(R.id.account_personal_change_password)
        personalInfoLayout = view.findViewById(R.id.account_personal_personal_information)

        passwordLayout = view.findViewById(R.id.account_personal_password)
        addNewPassword = view.findViewById(R.id.account_personal_add_password_button)

        fullNameLayout = view.findViewById(R.id.account_personal_personal_information_fullName)
        birthdateLayout = view.findViewById(R.id.account_personal_personal_information_birthdate)
        phoneNumberLayout = view.findViewById(R.id.account_personal_personal_information_phone)
        sexGroup = view.findViewById(R.id.account_personal_personal_information_sex)
        addPersonalInfo = view.findViewById(R.id.account_personal_personal_information_add_button)
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
        addNewPassword.setOnClickListener(null)
        addPersonalInfo.setOnClickListener(null)

        changePasswordLayout?.removeAllViews()
        personalInfoLayout?.removeAllViews()

        changePasswordLayout = null
        personalInfoLayout = null
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
        mainActivityModel.getUser().observe(viewLifecycleOwner) { observedUser ->
            user = observedUser.copy()
        }
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

        addNewPassword.setOnClickListener {
            val password = passwordLayout.editText?.text.toString().trim()
            passwordLayout.error = null
            viewModel.confirmPassword(password)
        }

        addPersonalInfo.setOnClickListener {
            val fullName = fullNameLayout.editText?.text.toString()
            val birthdate = birthdateLayout.editText?.text.toString().trim()
            val phoneNumber = phoneNumberLayout.editText?.text.toString().trim()
            deletePersonalInfo()
            viewModel.savePersonalInfo(fullName, birthdate, phoneNumber, sex)
        }

        birthdateLayout.editText?.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus)
            {
                MaterialDialog(activity!!).show {
                    datePicker { _, datetime ->
                        val dateText = datetime.toMyString()
                        birthdateLayout.editText?.setText(dateText)
                    }
                }
            }
        }
    }

    private fun isPersonalInformationSaved()
    {
        viewModel.isPersonalInfoChanged().observe(viewLifecycleOwner) { state ->
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
        }
    }

    private fun confirmBirthdate()
    {
        viewModel.confirmBirthdate().observe(viewLifecycleOwner) { birthdateState ->
            if(birthdateState is InvalidBirthdate)
                birthdateLayout.error = birthdateState.errorMessage
        }
    }

    private fun confirmPhoneNumber()
    {
        viewModel.confirmPhoneNumber().observe(viewLifecycleOwner) { phoneUmberState ->
            if(phoneUmberState is InvalidPhoneNumber)
                phoneNumberLayout.error = phoneUmberState.errorMessage
        }
    }

    private fun isPasswordChanged()
    {
        viewModel.isPasswordChanged().observe(viewLifecycleOwner) { success ->
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
