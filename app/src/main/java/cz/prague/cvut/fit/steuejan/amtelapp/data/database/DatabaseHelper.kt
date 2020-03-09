package cz.prague.cvut.fit.steuejan.amtelapp.data.database

object DatabaseHelper
{
    fun prepareCzechOrdering(name: String, surname: String): Pair<String, String>
    {
        val convertedName = changeFirstLetterToEnglishForm(name)
        val convertedSurname = changeFirstLetterToEnglishForm(surname)

        return Pair(convertedName, convertedSurname)
    }

    private fun changeFirstLetterToEnglishForm(text: String): String
    {
        return when(text[0].toLowerCase())
        {
            'č' -> "czzz${text.substring(1)}"
            'c' ->
            {
                if(text[1].toLowerCase() == 'h') "hzzz${text.substring(2)}"
                else text
            }
            'ď' -> "dzzz${text.substring(1)}"
            'ř' -> "rzzz${text.substring(1)}"
            'š' -> "szzz${text.substring(1)}"
            'ť' -> "tzzz${text.substring(1)}"
            'ž' -> "zzzz${text.substring(1)}"
            else -> text
        }
    }
}