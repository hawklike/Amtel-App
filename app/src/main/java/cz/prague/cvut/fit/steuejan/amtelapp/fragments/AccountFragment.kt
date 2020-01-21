package cz.prague.cvut.fit.steuejan.amtelapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        //TODO: change layout back to fragment_account
        return inflater.inflate(R.layout.account_boos_add_tm, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setToolbarTitle(getString(R.string.account))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        //TODO: change a layout based on user status
        val user = arguments?.get(DATA) as FirebaseUser
        Toast.makeText(activity, user.email, Toast.LENGTH_SHORT).show()
    }
}