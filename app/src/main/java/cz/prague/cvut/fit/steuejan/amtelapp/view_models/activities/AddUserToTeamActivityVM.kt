package cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Message
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.UserRepository
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.firstLetterUpperCase
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toCalendar
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toDate
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.Sex
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch
import java.util.*

class AddUserToTeamActivityVM : ViewModel()
{
    private val nameState = MutableLiveData<NameState>()
    fun confirmName(): LiveData<NameState> = nameState

    /*---------------------------------------------------*/

    private val surnameState = MutableLiveData<SurnameState>()
    fun confirmSurname(): LiveData<SurnameState> = surnameState

    /*---------------------------------------------------*/

    private val emailState = MutableLiveData<EmailState>()
    fun confirmEmail(): LiveData<EmailState> = emailState

    /*---------------------------------------------------*/

    private val birthdateState = MutableLiveData<BirthdateState>()
    fun confirmBirthdate(): LiveData<BirthdateState> = birthdateState

    /*---------------------------------------------------*/

    private val teamState = SingleLiveEvent<TeamState>()
    fun isUserAdded(): LiveData<TeamState> = teamState

    /*---------------------------------------------------*/

    /*
    Adds a new user into the team given as an argument.
     */
    fun addUser(name: String, surname: String, email: String, birthdate: String, sex: Sex, team: Team)
    {
        if(confirmUser(name, surname, email, birthdate))
        {
            viewModelScope.launch {
                //create new user
                var user: User? = User(
                    null,
                    name.firstLetterUpperCase(),
                    surname.firstLetterUpperCase(),
                    email,
                    null,
                    birthdate.toDate(),
                    sex.toBoolean(),
                    UserRole.PLAYER.toString(),
                    team.id,
                    team.name
                )

                //upload a user to a database
                user = UserRepository.setUser(user!!)

                //checks if the upload was successful
                if(user != null)
                {
                    //add the new user into the team
                    if(team.users.add(user) && team.usersId.add(user.id!!))
                    {
                        //update the team in database, the team entity has two fields which care about team users
                        TeamRepository.updateTeam(team.id, mapOf("users" to team.users, "usersId" to team.usersId))
                        teamState.value = ValidTeam(team)
                    }
                    else teamState.value = NoTeam
                }
                else teamState.value = NoTeam
            }
        }
    }

    private fun confirmUser(name: String, surname: String, email: String, birthdate: String): Boolean
    {
        var okName = true
        var okSurname = true
        var okEmail = true
        var okBirthdate = true

        if(name.isEmpty())
        {
            nameState.value = InvalidName()
            okName = false
        }

        if(surname.isEmpty())
        {
            surnameState.value = InvalidSurname()
            okSurname = false
        }

        with(EmailState.validate(email)) {
            if(this is InvalidEmail)
            {
                okEmail = false
                emailState.value = this
            }
        }

        with(BirthdateState.validate(birthdate)) {
            if(this is InvalidBirthdate)
            {
                okBirthdate = false
                birthdateState.value = this
            }
        }

        return okName && okSurname && okEmail && okBirthdate
    }

    /*
    Sets a text which is about to be placed within a dialog.
     */
    fun createDialog(teamState: TeamState): Message
    {
        return if(teamState is ValidTeam) Message(
            App.context.getString(R.string.add_user_success_message_title),
            null
        )
        else Message(
            App.context.getString(
                R.string.add_user_failure_message_title
            ), null
        )
    }

    /*
    If the birthdate is set, convert it into type Calendar.
     */
    fun setDialogBirthdate(birthdate: Editable): Calendar?
    {
        return if(birthdate.isEmpty()) null
        else birthdate.toString().toCalendar()
    }

}