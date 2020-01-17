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
        state.set(DRAWER_POSITON, position)
    }

    fun getDrawerSelectedPosition(): Int = state[DRAWER_POSITON] ?: 0

    companion object
    {
        const val TITLE = "title"
        const val DRAWER_POSITON = "position"
    }
}