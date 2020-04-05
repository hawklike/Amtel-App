package cz.prague.cvut.fit.steuejan.amtelapp.view_models

import androidx.lifecycle.ViewModel
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.TeamOrderBy

class TeamsFragmentVM : ViewModel()
{
    var orderBy = TeamOrderBy.NAME
    var query = TeamManager.retrieveAllTeams()
}
