package cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts

import androidx.fragment.app.activityViewModels
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.activities.ScheduleRoundsActivityVM

abstract class AbstractScheduleActivityFragment : AbstractBaseFragment()
{
    protected val scheduleViewModel by activityViewModels<ScheduleRoundsActivityVM>()
}