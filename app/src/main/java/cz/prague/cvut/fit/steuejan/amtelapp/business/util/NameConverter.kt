package cz.prague.cvut.fit.steuejan.amtelapp.business.util

object NameConverter
{
    //TODO: uncomment this
    fun getRandomString(length: Int) : String
    {
//        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz1234567890"
//        return (1..length)
//            .map { allowedChars.random() }
//            .joinToString("")

        return "123456"
    }
}

fun String.firstLetterUpperCase(): String
{
    val output = this.toLowerCase()
    return output.replaceRange(0, 1,
        this[0]
            .toUpperCase()
            .toString())
}