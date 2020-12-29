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
class GomokuPlayer(val name : String, val color : GomokuFieldColor) {

    private var placed = 0
    private var stones = mapOf<Int, Array<Int>>()
    private var maximum = 0
    private var wins = 0
    private var streak = 0

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

        // case: game ended -> update win and streak count
        won?.let {
            if (won) {
                wins++; streak++
            } else {
                streak = 0
            }; return
        }

        // increase placed counter and add position to map
        stones = stones + Pair(++placed, position)

        // update the longest line found
        maximum = longestLine
    }

    // TODO: remove debugging function
    override fun toString() : String {
        return "$name has $color stones and placed $placed stones yet (max: $maximum - wins: $wins - streak: $streak)"
    }
}