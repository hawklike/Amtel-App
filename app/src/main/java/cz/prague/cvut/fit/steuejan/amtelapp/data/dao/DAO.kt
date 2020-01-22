package cz.prague.cvut.fit.steuejan.amtelapp.data.dao

import com.google.android.gms.tasks.Task

interface DAO
{
    fun <T> insert(entity: T): Task<Void>
}
