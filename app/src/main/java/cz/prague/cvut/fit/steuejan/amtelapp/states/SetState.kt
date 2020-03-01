package cz.prague.cvut.fit.steuejan.amtelapp.states

sealed class SetState
{
    companion object
    {
        fun validate(score: String, optional: Boolean = false): SetState
        {
            if(score.isEmpty() && !optional) return InvalidSet("Zadejte prosím počet získaných gemů.")

            var games = 0
            try { games = score.toInt() }
            catch(ex: NumberFormatException) { if(!optional) return InvalidSet("Zadejte prosím skóre v číselné podobě.")}

            if(games < 0) return InvalidSet("Počet gemů nesmí být záporné číslo.")

            return ValidSet(games)
        }

        fun validate(gamesHome: Int, gamesAway: Int): Boolean
        {
            if(gamesHome == 6 && gamesAway < 5) return true
            if(gamesAway == 6 && gamesHome < 5) return true
            if(gamesHome == 7 && (gamesAway == 6 || gamesAway == 5)) return true
            if(gamesAway == 7 && (gamesHome == 6 || gamesHome == 5)) return true
            return false
        }
    }
}
data class ValidSet(val self: Int) : SetState()
data class InvalidSet(val errorMessage: String) : SetState()