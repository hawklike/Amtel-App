package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R

sealed class PlaceState
data class ValidPlace(val place: String) : PlaceState()
data class InvalidPlace(val errorMessage: String = App.context.getString(R.string.create_team_failure_place_error)) : PlaceState()