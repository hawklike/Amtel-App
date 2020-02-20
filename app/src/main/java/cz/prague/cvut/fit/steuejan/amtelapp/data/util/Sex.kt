package cz.prague.cvut.fit.steuejan.amtelapp.data.util

enum class Sex
{
    MAN, WOMAN;

    fun toBoolean(): Boolean
    {
        return when(this)
        {
            MAN -> true
            WOMAN -> false
        }
    }
}

fun Boolean.toSex(): Sex
{
    return when(this)
    {
        true -> Sex.MAN
        false -> Sex.WOMAN
    }
}