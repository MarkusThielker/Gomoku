package de.markus_thielker.gomoku.components

/**
 * The GomokuPlayer class saves all the information needed for background stats and in game UI.
 * It contains only one method for updating its state to keep the communication clean and simple.
 *
 * Every GomokuGame needs exactly two GomokuPlayers to work.
 *
 * @param name is a read only string
 * @param color is a read only GomokuFieldEnum object setting the players stone color
 *
 * @author Markus Thielker
 *
 */
class GomokuPlayer(val name : String, var color : GomokuFieldColor) {

    var placed = 0
    var stones = mapOf<Int, Array<Int>>()
    var maximum = 0
    var wins = 0
    var streak = 0

    /**
     * Update player stats after stone was placed.
     *
     * @param position arrayOf(x,y) position of last placed stone to undo moves
     * @param longestLine pass the max length of stones in a row
     * @param won pass if player has won the game (true = won, false = lost, null = not done)
     *
     * @author Markus Thielker
     *
     * */
    fun updateState(position : Array<Int>, longestLine : Int, won : Boolean?) {

        // increase placed counter and add position to map
        if (position[0] >= 0) stones = stones + Pair(++placed, position)

        // update the longest line found
        if (longestLine > 0) maximum = longestLine

        // case: game ended -> update win and streak count
        won?.let {
            if (won) {
                // increase streak and win count
                wins++; streak++
            } else {
                // reset streak to zero
                streak = 0
            }; return
        }
    }

    /**
     * This function is called when an rematch is started to clear the stats.
     *
     * @author Markus Thielker
     *
     * */
    fun clearStats() {

        placed = 0
        stones = mapOf()
        maximum = 0
    }
}