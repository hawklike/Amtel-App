package cz.prague.cvut.fit.steuejan.amtelapp.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MainActivityModel(private val state: SavedStateHandle) : ViewModel()
{
    fun setTitle(title: String)
    {
        state.set(TITLE, title)
    }

    fun getTitle(): LiveData<String> = state.getLiveData(TITLE)

    fun setDrawerSelectedPosition(position: Int)
    {
        state.set(DRAWER_POSITION, position)
    }

    fun getDrawerSelectedPosition(): Int = state[DRAWER_POSITION] ?: 0

    companion object
    {
        private const val TITLE = "title"
        private const val DRAWER_POSITION = "position"
    }
}