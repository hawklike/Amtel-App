package cz.prague.cvut.fit.steuejan.amtelapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseUser
import cz.prague.cvut.fit.steuejan.amtelapp.R

class AccountFragment : AbstractBaseFragment()
{
    companion object
    {
        const val DATA = "user"

        //TODO: change to a User
        fun newInstance(user: FirebaseUser): AccountFragment
        {
            val fragment = AccountFragment()
            val bundle = Bundle()
            bundle.putParcelable(DATA, user)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val user = arguments?.get(DATA)
        //TODO: change a layout based on user status
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setToolbarTitle(getString(R.string.account))
    }
}