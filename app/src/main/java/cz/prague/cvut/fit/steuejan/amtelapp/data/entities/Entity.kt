package cz.prague.cvut.fit.steuejan.amtelapp.data.entities

abstract class Entity<T> : Comparable<Entity<T>>
{
    open var id: String? = null

    override fun compareTo(other: Entity<T>): Int =
        compareBy<Entity<T>>{ it.id }.compare(this, other)
}