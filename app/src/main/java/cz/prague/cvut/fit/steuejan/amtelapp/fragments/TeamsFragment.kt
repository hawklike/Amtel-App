package cz.prague.cvut.fit.steuejan.amtelapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractMainActivityFragment

class TeamsFragment : AbstractMainActivityFragment()
{
    companion object
    {
        fun newInstance(): TeamsFragment = TeamsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_teams, container, false)
    }

    override fun getName(): String = "TeamsFragment"

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        setToolbarTitle(getString(R.string.teams))
    }
}