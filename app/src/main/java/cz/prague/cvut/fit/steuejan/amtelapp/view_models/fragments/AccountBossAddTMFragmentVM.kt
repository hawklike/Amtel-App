package cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments

import android.preference.PreferenceManager
import android.text.Editable
import androidx.lifecycle.*
import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.AuthManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.helpers.SingleLiveEvent
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.*
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Team
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.LeagueRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.UserRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.UserRole
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.toRole
import cz.prague.cvut.fit.steuejan.amtelapp.states.*
import kotlinx.coroutines.launch
import java.util.*

class AccountBossAddTMFragmentVM : ViewModel()
{
    var chosenTeam: Team? = null

    /*---------------------------------------------------*/

    private val _registration = SingleLiveEvent<RegistrationState>()
    val registration: LiveData<RegistrationState> = _registration

    /*---------------------------------------------------*/

    private val _credentials = SingleLiveEvent<CredentialsState>()
    val credentials: LiveData<CredentialsState> = _credentials

    /*---------------------------------------------------*/

    private val _deadlineAdded = SingleLiveEvent<Boolean>()
    val isDeadlineAdded: LiveData<Boolean> = _deadlineAdded

    /*---------------------------------------------------*/

    private val _deadlineDeleted = SingleLiveEvent<Boolean>()
    val isDeadlineDeleted: LiveData<Boolean> = _deadlineDeleted

    /*---------------------------------------------------*/

    private val _team  = MutableLiveData<List<Team>>()
    val teams: LiveData<List<Team>> = _team

    /*---------------------------------------------------*/

    private val _deadline = SingleLiveEvent<Pair<String?, String?>>()
    val deadline: LiveData<Pair<String?, String?>> = _deadline

    /*---------------------------------------------------*/

    fun createUser(credentials: ValidCredentials)
    {
        val password = StringUtil.getRandomString(6)

        viewModelScope.launch {
            val uid = AuthManager.signUpUser(credentials.email, password)

            uid?.let {
                val (name, surname, email) = credentials
                val user = User(uid, name, surname, email, role = UserRole.TEAM_MANAGER.toString())

                chosenTeam?.let { team ->
                    user.teamId = team.id
                    user.teamName = team.name
                }

                UserRepository.setUser(user)?.let {
                    EmailSender.sendEmail(
                        email,
                        context.getString(R.string.verificationEmail_subject),
                        createVerificationTemplate(email, password))
                    addToExistingTeam(user)
                    _registration.value = ValidRegistration
                } ?: let { _registration.value = InvalidRegistration }
            } ?: let { _registration.value = InvalidRegistration }
        }
    }

    private suspend fun addToExistingTeam(user: User)
    {
        chosenTeam?.let { team ->
            val previousTM = team.tmId

            //update a team manager in team
            team.tmId = user.id!!
            team.usersId.add(user.id!!)

            //update team's users
            team.users.forEach { user ->
                if(user.role.toRole() == UserRole.TEAM_MANAGER)
                    user.role = UserRole.PLAYER.toString()
            }
            team.users.add(user)

            UserRepository.updateUser(previousTM, mapOf("role" to  UserRole.PLAYER.toString()))
            TeamRepository.setTeam(team)
        }
    }

    fun confirmCredentials(name: String, surname: String, email: String)
    {
        var okName = true
        var okSurname = true
        var okEmail = true

        var cName = ""
        var cSurname = ""

        if(name.isNotEmpty()) cName = name.firstLetterUpperCase()
        else okName = false

        if(surname.isNotEmpty()) cSurname = surname.firstLetterUpperCase()
        else okSurname = false

        if(EmailState.validate(email) is InvalidEmail) okEmail = false

        if(okName && okSurname && okEmail) _credentials.value = ValidCredentials(cName, cSurname, email)
        else _credentials.value = InvalidCredentials(okName, okSurname, okEmail)
    }

    private fun createVerificationTemplate(email: String, password: String): String
    {
        val head = context.getString(R.string.autoEmail_template_head)
        val body = "email: $email\nheslo: $password\n\n"
        val foot = context.getString(R.string.autoEmail_template_foot)
        return "$head$body$foot"
    }

    fun setDialogDeadline(birthdate: Editable): Calendar?
    {
        return if(birthdate.isEmpty()) null
        else birthdate.toString().toCalendar()
    }

    fun setDeadline(deadline: Date, from: Boolean)
    {
        viewModelScope.launch {
            with(LeagueRepository.setDeadline(deadline, from)) {
                _deadlineAdded.value = this
                if(this) setDeadlineInPreferences(deadline.toMyString(), from)
            }
        }
    }

    fun getDeadline()
    {
        val deadlineFrom = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString("DEADLINE_FROM", null)

        val deadlineTo = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString("DEADLINE_TO", null)

        if(deadlineFrom == null || deadlineTo == null)
        {
            viewModelScope.launch {
                val result = LeagueRepository.getDeadline()
                result?.let {
                    setDeadlineInPreferences(it.first?.toMyString(), true)
                    setDeadlineInPreferences(it.second?.toMyString(), false)
                    _deadline.value = Pair(it.first?.toMyString(), it.second?.toMyString())
                }
            }
        }
        else _deadline.value = Pair(deadlineFrom, deadlineTo)
    }

    private fun setDeadlineInPreferences(deadline: String?, from: Boolean)
    {
        val label =
            if(from) "DEADLINE_FROM"
            else "DEADLINE_TO"

        PreferenceManager.
            getDefaultSharedPreferences(context)
            .edit()
            .putString(label, deadline)
            .apply()
    }

    fun deleteDeadline()
    {
        viewModelScope.launch {
            val ok1 = LeagueRepository.setDeadline(null, true)
            val ok2 = LeagueRepository.setDeadline(null, false)
            setDeadlineInPreferences(null, true)
            setDeadlineInPreferences(null, false)
            _deadlineDeleted.value = ok1 && ok2
        }
    }

    fun retrieveAllTeams()
    {
        viewModelScope.launch {
            _team.value = TeamRepository.retrieveAllTeams()
        }
    }
}