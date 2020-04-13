package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.toMyString
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.User
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.EditUserBinding

class EditUserActivity : AbstractBaseActivity()
{
    private lateinit var binding: EditUserBinding

    private var user: User? = null

    companion object
    {
        const val USER = "user"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        binding = EditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        getUser()
        setToolbarTitle("Upravit hráče")
        setArrowBack()
        populateField()
    }

    private fun getUser()
    {
        intent.extras?.let { bundle ->
            user = bundle.getParcelable(USER)
        }
    }

    private fun populateField()
    {
        user?.let {
            with(binding) {
                name.editText?.setText(it.name)
                surname.editText?.setText(it.surname)
                email.editText?.setText(it.email)
                phone.editText?.setText(it.phone)
                birthdate.editText?.setText(it.birthdate?.toMyString())
            }
        }
    }
}