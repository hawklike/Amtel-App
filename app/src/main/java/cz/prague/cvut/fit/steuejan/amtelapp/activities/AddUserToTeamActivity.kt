package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Sex
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.AddUserToTeamActivityVM

class AddUserToTeamActivity : AbstractBaseActivity()
{
    private lateinit var team: Team

    private val viewModel by viewModels<AddUserToTeamActivityVM>()

    private lateinit var nameLayout: TextInputLayout
    private lateinit var surnameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var birthdateLayout: TextInputLayout
    private lateinit var sexGroup: RadioGroup
    private lateinit var addButton: FloatingActionButton

    companion object
    {
        const val TEAM = "team"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.account_add_user_to_team_acitivity)
        super.onCreate(savedInstanceState)
        setToolbarTitle("Přidat hráče")
        setArrowBack()

        intent.extras?.let { team = it.getParcelable(TEAM)!! }

        nameLayout = findViewById(R.id.account_add_user_to_team_name)
        surnameLayout = findViewById(R.id.account_add_user_to_team_surname)
        emailLayout = findViewById(R.id.account_add_user_to_team_email)
        birthdateLayout = findViewById(R.id.account_add_user_to_team_birthdate)
        sexGroup = findViewById(R.id.account_add_user_to_team_sex)
        addButton = findViewById(R.id.account_add_user_to_team_add_button)

        setListeners()
    }

    private fun setListeners()
    {
        var sex = Sex.MAN
        sexGroup.setOnCheckedChangeListener { _, checkedId ->
            val rb = findViewById<RadioButton>(checkedId)
            sex = if(rb.id == R.id.account_personal_personal_information_sex_man) Sex.MAN else Sex.WOMAN
        }

        addButton.setOnClickListener {
            val name = nameLayout.editText?.text
            val surname = surnameLayout.editText?.text
            val email = emailLayout.editText?.text
            val birthdate = birthdateLayout.editText?.text

//            viewModel.addUser(
//                name,
//                surname,
//                email,
//                birthdate,
//                sex,
//                "")
        }
    }

    override fun onBackPressed()
    {
        super.onBackPressed()
        finish()
    }
}