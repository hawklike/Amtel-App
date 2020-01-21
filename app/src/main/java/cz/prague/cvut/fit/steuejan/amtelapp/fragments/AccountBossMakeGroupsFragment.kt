package cz.prague.cvut.fit.steuejan.amtelapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseUser
import cz.prague.cvut.fit.steuejan.amtelapp.R

class AccountBossMakeGroupsFragment : AbstractBaseFragment()
{
    companion object
    {
        const val DATA = "user"

        //TODO: change to a User
        fun newInstance(user: FirebaseUser): AccountBossMakeGroupsFragment
        {
            val fragment = AccountBossMakeGroupsFragment()
            val bundle = Bundle()
            bundle.putParcelable(DATA, user)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.dummy_layout, container, false)
    }
}