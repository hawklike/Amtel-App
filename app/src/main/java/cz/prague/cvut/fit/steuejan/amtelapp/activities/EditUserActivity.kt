package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.app.Activity
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.datetime.datePicker
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.toast
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.LeagueRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole.PLAYER
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole.TEAM_MANAGER
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.EditUserBinding
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities.EditUserActivityVM

class EditUserActivity : AbstractBaseActivity()
{
    private lateinit var binding: EditUserBinding

    private val viewModel by viewModels<EditUserActivityVM>()

    private val progressDialog by lazy {
        MaterialDialog(this)
            .customView(R.layout.progress_layout)
    }

    companion object
    {
        const val USER = "user"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        binding = EditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        setUser()
        setToolbarTitle(getString(R.string.edit_player))
        setArrowBack()
        populateField()
        changeBirthday()
        changeRole()
        confirmInput()
        saveUser()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        with(binding) {
            birthdate.editText?.setOnClickListener(null)
            changeRoleButton.setOnClickListener(null)
            saveButton.setOnClickListener(null)
        }
    }

    private fun setUser()
    {
        intent.extras?.let { bundle ->
            viewModel.user = bundle.getParcelable(USER)
        }
    }

    private fun populateField()
    {
        viewModel.user?.let {
            with(binding) {
                name.editText?.setText(it.name)
                surname.editText?.setText(it.surname)
                email.editText?.setText(it.email)
                phone.editText?.setText(it.phone)
                birthdate.editText?.setText(it.birthdate?.toMyString())
                if(it.role.toRole() == PLAYER) phone.visibility = GONE
            }
            displayChangeRoleButton(it)
        }
    }

    /*
    If the logged in user is a head of league, display change role button.
     */
    private fun displayChangeRoleButton(user: User)
    {
        if(AuthManager.currentUser?.uid != null && AuthManager.currentUser?.uid == LeagueRepository.headOfLeague?.id)
        {
            if(user.role.toRole() == TEAM_MANAGER) binding.changeRoleButton.visibility = VISIBLE
            else binding.changeRoleButton.visibility = GONE
        }
        else binding.changeRoleButton.visibility = GONE
    }

    private fun changeBirthday()
    {
        binding.birthdate.editText?.setOnClickListener {
            MaterialDialog(this).show {
                val savedDate = binding.birthdate.editText?.text?.let {
                    viewModel.setDialogBirthdate(it)
                }

                datePicker(currentDate = savedDate) { _, date ->
                    val dateText = date.toMyString()
                    binding.birthdate.editText?.setText(dateText)
                }
            }
        }
    }

    /*
    Only enabled for team managers and visible for a head of league.
    After clicking on the change role button, team manager losses his role
    and is converted into an ordinary user.
     */
    private fun changeRole()
    {
        with(binding) {
            changeRoleButton.setOnClickListener {
                MaterialDialog(this@EditUserActivity).show {
                    title(text = getString(R.string.delete_role_confirmation))
                    message(R.string.delete_role_message)
                    positiveButton(R.string.delete) {
                        viewModel.deleteRole = true
                    }
                    negativeButton()
                }
            }
        }
    }

    private fun confirmInput()
    {
        confirmName()
        confirmSurname()
        confirmEmail()
        confirmPhoneNumber()
        confirmBirthdate()
    }

    private fun confirmName()
    {
        viewModel.name.observe(this) { name ->
            if(name is InvalidName)
                binding.name.error = name.errorMessage
            progressDialog.dismiss()
        }
    }

    private fun confirmSurname()
    {
        viewModel.surname.observe(this) { surname ->
            if(surname is InvalidSurname)
                binding.surname.error = surname.errorMessage
            progressDialog.dismiss()
        }
    }

    private fun confirmEmail()
    {
        viewModel.email.observe(this) { email ->
            if(email is InvalidEmail)
                binding.email.error = email.errorMessage
            progressDialog.dismiss()
        }
    }

    private fun confirmPhoneNumber()
    {
        viewModel.phoneNumber.observe(this) { phoneNumber ->
            if(phoneNumber is InvalidPhoneNumber)
                binding.phone.error = phoneNumber.errorMessage
            progressDialog.dismiss()
        }
    }

    private fun confirmBirthdate()
    {
        viewModel.birthdate.observe(this) { birthdate ->
            if(birthdate is InvalidBirthdate)
                binding.birthdate.error = birthdate.errorMessage
            progressDialog.dismiss()
        }
    }

    private fun saveUser()
    {
        with(binding) {
            saveButton.isClickable = true
            saveButton.backgroundTintList = ColorStateList.valueOf(App.getColor(R.color.blue))
            saveButton.setOnClickListener {
                progressDialog.show()
                clearErrors()
                val name = name.editText?.text.toString().trim()
                val surname = surname.editText?.text.toString().trim()
                val email = email.editText?.text.toString().trim()
                val phone = phone.editText?.text.toString()
                val birthdate = birthdate.editText?.text.toString().trim()
                viewModel.editUser(name, surname, email, phone, birthdate)
            }
        }

        //invoke when a user is edited
        viewModel.userEdited.observe(this) { success ->
            progressDialog.dismiss()
            if(success)
            {
                toast(R.string.success_long)
                setResult(Activity.RESULT_OK)
                onBackPressed()
            }
            else toast(R.string.failure_long)
        }
    }

    private fun clearErrors()
    {
        with(binding)
        {
            name.error = null
            surname.error = null
            email.error = null
            phone.error = null
            birthdate.error = null
        }
    }
}