package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.lifecycle.observe
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.LeagueRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole.TEAM_MANAGER
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.EditUserBinding
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
        const val TEAM = "team"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        binding = EditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        getData()
        getTeam()
        setToolbarTitle("Upravit hráče")
        setArrowBack()
        populateField()
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

    private fun getData()
    {
        intent.extras?.let { bundle ->
            viewModel.user = bundle.getParcelable(USER)
            viewModel.team = bundle.getParcelable(TEAM)
        }

       viewModel.getTeam()
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
            }
            displayChangeRoleButton(it)
        }
    }

    private fun displayChangeRoleButton(user: User)
    {
        if(AuthManager.currentUser?.uid != null && AuthManager.currentUser?.uid == LeagueRepository.headOfLeague?.id)
        {
            if(user.role.toRole() == TEAM_MANAGER) binding.changeRoleButton.visibility = VISIBLE
            else binding.changeRoleButton.visibility = GONE
        }
        else binding.changeRoleButton.visibility = GONE
    }

    private fun getTeam()
    {
        viewModel.teamLoaded.observe(this) { loaded ->
            if(loaded)
            {
                changeRole()
                saveUser()
            }
        }
    }

    private fun changeRole()
    {
        with(binding) {
            changeRoleButton.isClickable = true
            changeRoleButton.setTextColor(App.getColor(R.color.blue))

            changeRoleButton.setOnClickListener {
                viewModel.team?.let { team ->
                    val emails = team.users.map { it.email }.toMutableList()
                    emails.remove(viewModel.user?.email ?: "")

                    MaterialDialog(this@EditUserActivity).show {
                        title(text = "Předat roli jinému hráči")
                        listItemsSingleChoice(initialSelection = viewModel.emailIdx, items = emails) { _, index, text ->
                            viewModel.chosenEmail = text.toString()
                            viewModel.emailIdx = index
                        }
                        positiveButton()
                        negativeButton()
                    }
                }
            }
        }
    }

    private fun saveUser()
    {
        with(binding) {
            saveButton.isClickable = true
            saveButton.backgroundTintList = ColorStateList.valueOf(App.getColor(R.color.blue))
        }
    }

}