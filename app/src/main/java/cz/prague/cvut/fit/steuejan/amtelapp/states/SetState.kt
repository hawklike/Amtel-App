package cz.prague.cvut.fit.steuejan.amtelapp.states

import cz.prague.cvut.fit.steuejan.amtelapp.App.Companion.context
import cz.prague.cvut.fit.steuejan.amtelapp.R

sealed class SetState
{
    companion object
    {
        //TODO: test validation
        fun validate(score: String, optional: Boolean, isFiftyGroup: Boolean): SetState
        {
            if(score.isEmpty() && !optional) return InvalidSet(context.getString(R.string.gems_empty_error))

            var games = Int.MAX_VALUE
            try { games = score.toInt() }
            catch(ex: NumberFormatException) { if(!optional) return InvalidSet(context.getString(R.string.gems_not_number_error))}

            if(games < 0) 
                return InvalidSet(context.getString(R.string.too_little_gems_error))
            if(games > 7 && games != Int.MAX_VALUE && !isFiftyGroup)
                return InvalidSet(String.format(context.getString(R.string.too_many_gems_error), 7))
            if(games > 10 && games != Int.MAX_VALUE && isFiftyGroup)
                return InvalidSet(String.format(context.getString(R.string.too_many_gems_error), 10))

            return ValidSet(games)
        }

        fun validate(gamesHome: Int, gamesAway: Int): Boolean
        {
            if(gamesHome == Int.MAX_VALUE || gamesAway == Int.MAX_VALUE) return false
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