package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers

import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.GroupManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.TeamManager
import cz.prague.cvut.fit.steuejan.amtelapp.business.util.DateUtil
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Group
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroup
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidGroups
import cz.prague.cvut.fit.steuejan.amtelapp.states.ValidTeams

class SeasonFinisher(private val groups: MutableList<Group>)
{


    suspend fun createPlayoff(): Boolean
    {
        val playOff = Group(null, App.context.getString(R.string.playOff), playingPlayOff = false, playOff = true, rank = Int.MAX_VALUE)
        return GroupManager.addPlayoff(playOff) is ValidGroup
    }

    suspend fun updateTeamRanks()
    {
        if(groups.isEmpty())
        {
            with(GroupManager.retrieveAllGroupsExceptPlayoff()) {
                if(this is ValidGroups) groups.addAll(self)
            }
        }

        groups.forEach { group ->
            with(GroupManager.retrieveTeamsInGroup(group.id)) {
                if(this is ValidTeams)
                {
                    val rankedTeams = RankingSolver(self, DateUtil.actualYear.toInt()).sort()
                    for(i in rankedTeams.indices)
                    {
                        rankedTeams[i].results.add(i + 1)
                        TeamManager.addTeam(rankedTeams[i])
                    }
                }
            }
        }
    }
}