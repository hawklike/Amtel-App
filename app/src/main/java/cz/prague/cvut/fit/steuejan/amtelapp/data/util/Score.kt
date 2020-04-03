package cz.prague.cvut.fit.steuejan.amtelapp.data.util

data class Score(var sets: Int = 0, var games: Int = 0) : Comparable<Score>
{
    override fun compareTo(other: Score): Int =
        compareByDescending<Score>{ it.sets }.thenByDescending{ it.games }.compare(this, other)

    override fun toString(): String = "($sets $games)"
}