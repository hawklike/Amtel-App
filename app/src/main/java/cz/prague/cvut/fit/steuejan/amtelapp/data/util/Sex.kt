package cz.prague.cvut.fit.steuejan.amtelapp.data.util

enum class Sex
{
    MAN, WOMAN;

    companion object
    {
        fun toBoolean(sex: Sex): Boolean
        {
            return when(sex)
            {
                MAN -> true
                WOMAN -> false
            }
        }

        fun toSex(boolean: Boolean): Sex
        {
            return when(boolean)
            {
                true -> MAN
                false -> WOMAN
            }
        }
    }
}