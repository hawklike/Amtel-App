package cz.prague.cvut.fit.steuejan.amtelapp.states

sealed class PlaceState
data class ValidPlace(val place: String) : PlaceState()
data class InvalidPlace(val errorMessage: String) : PlaceState()