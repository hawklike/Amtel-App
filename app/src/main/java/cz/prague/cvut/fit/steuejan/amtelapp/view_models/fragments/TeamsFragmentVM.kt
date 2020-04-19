package cz.prague.cvut.fit.steuejan.amtelapp.view_models.fragments

import androidx.lifecycle.ViewModel
import cz.prague.cvut.fit.steuejan.amtelapp.data.repository.TeamRepository
import cz.prague.cvut.fit.steuejan.amtelapp.data.util.TeamOrderBy

class TeamsFragmentVM : ViewModel()
{
    var orderBy = TeamOrderBy.NAME
    var query = TeamRepository.retrieveAllTeamsQuery()
}
