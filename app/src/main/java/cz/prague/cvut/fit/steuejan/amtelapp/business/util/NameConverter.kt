package cz.prague.cvut.fit.steuejan.amtelapp.business.util

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

    fun getRandomString(length: Int) : String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz1234567890"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}