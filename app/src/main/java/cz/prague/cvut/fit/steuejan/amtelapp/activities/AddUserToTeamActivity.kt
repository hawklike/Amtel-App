package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.app.Activity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.viewModels
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.datetime.datePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.shrinkWhitespaces
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Sex
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities.AddUserToTeamActivityVM

class AddUserToTeamActivity : AbstractBaseActivity()
{
    private val viewModel by viewModels<AddUserToTeamActivityVM>()

    private var team = Team()

    private lateinit var nameLayout: TextInputLayout
    private lateinit var surnameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var birthdateLayout: TextInputLayout
    private lateinit var sexGroup: RadioGroup
    private lateinit var addButton: FloatingActionButton

    private val progressDialog by lazy {
        MaterialDialog(this)
            .customView(R.layout.progress_layout)
    }

    companion object
    {
        const val TEAM = "team"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.account_add_user_to_team_activity)
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.add_player))
        setArrowBack()

        intent.extras?.let { team = it.getParcelable(TEAM) ?: Team() }

        nameLayout = findViewById(R.id.account_add_user_to_team_name)
        surnameLayout = findViewById(R.id.account_add_user_to_team_surname)
        emailLayout = findViewById(R.id.account_add_user_to_team_email)
        birthdateLayout = findViewById(R.id.account_add_user_to_team_birthdate)
        sexGroup = findViewById(R.id.account_add_user_to_team_sex)
        addButton = findViewById(R.id.account_add_user_to_team_add_button)

        setListeners()
        setObservers()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        sexGroup.setOnCheckedChangeListener(null)
        addButton.setOnClickListener(null)
        birthdateLayout.editText?.onFocusChangeListener
    }

    private fun setListeners()
    {
        var sex = Sex.MAN
        sexGroup.setOnCheckedChangeListener { _, checkedId ->
            val rb = findViewById<RadioButton>(checkedId)
            sex = if(rb.id == R.id.account_personal_personal_information_sex_man) Sex.MAN else Sex.WOMAN
        }

        addButton.setOnClickListener {
            progressDialog.show()

            val name = nameLayout.editText?.text.toString().trim().shrinkWhitespaces()
            val surname = surnameLayout.editText?.text.toString().trim().shrinkWhitespaces()
            val email = emailLayout.editText?.text.toString().trim()
            val birthdate = birthdateLayout.editText?.text.toString().trim()

            deleteErrors()
            //add user to team
            viewModel.addUser(name, surname, email, birthdate, sex, team)
        }

        birthdateLayout.editText?.setOnClickListener {
            MaterialDialog(this).show {
                val savedDate = birthdateLayout.editText?.text?.let {
                    viewModel.setDialogBirthdate(it)
                }

                datePicker(currentDate = savedDate) { _, datetime ->
                    birthdateLayout.editText?.setText(datetime.toMyString())
                }
            }
        }
    }

    private fun setObservers()
    {
        confirmName()
        confirmSurname()
        confirmEmail()
        confirmBirthdate()
        isUserAdded()
    }

    private fun confirmName()
    {
        viewModel.confirmName().observe(this) { name ->
            if(name is InvalidName)
            {
                progressDialog.dismiss()
                nameLayout.error = name.errorMessage
            }
        }
    }

    private fun confirmSurname()
    {
        viewModel.confirmSurname().observe(this) { surname ->
            if(surname is InvalidSurname)
            {
                progressDialog.dismiss()
                surnameLayout.error = surname.errorMessage
            }
        }
    }

    private fun confirmEmail()
    {
        viewModel.confirmEmail().observe(this) { email ->
            if(email is InvalidEmail)
            {
                progressDialog.dismiss()
                emailLayout.error = email.errorMessage
            }
        }
    }

    private fun confirmBirthdate()
    {
        viewModel.confirmBirthdate().observe(this) { birthdate ->
            if(birthdate is InvalidBirthdate)
            {
                progressDialog.dismiss()
                birthdateLayout.error = birthdate.errorMessage
            }
        }
    }

    /*
    This method handles a situation when a user is added to team.
     */
    private fun isUserAdded()
    {
        viewModel.isUserAdded().observe(this) { teamState ->
            progressDialog.dismiss()
            val title = viewModel.createDialog(teamState).title

            if(teamState is ValidTeam)
            {
                deleteInput()
                setResult(Activity.RESULT_OK, intent.putExtra(TEAM, teamState.self))
            }

            MaterialDialog(this)
                .title(text = title)
                .show {
                    //when clicked on OK, go back to previous activity
                    positiveButton(R.string.ok) { this@AddUserToTeamActivity.onBackPressed() }
                }
        }
    }

    private fun deleteInput()
    {
        nameLayout.editText?.text?.clear()
        surnameLayout.editText?.text?.clear()
        emailLayout.editText?.text?.clear()
        birthdateLayout.editText?.text?.clear()
    }

    private fun deleteErrors()
    {
        nameLayout.error = null
        surnameLayout.error = null
        emailLayout.error = null
        birthdateLayout.error = null
    }
}