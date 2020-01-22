package cz.prague.cvut.fit.steuejan.amtelapp.business.utils

object NameConverter
{
    fun convertToFirstLetterBig(textToBeConverted: String): String
    {
        val output = textToBeConverted.toLowerCase()
        return output.replaceRange(0, 1,
            textToBeConverted[0]
                .toUpperCase()
                .toString())
    }
}