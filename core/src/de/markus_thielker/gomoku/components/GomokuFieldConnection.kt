package de.markus_thielker.gomoku.components

/**
 * Holds information for one connection on the game board
 *
 * @param from is the starting node
 * @param to is the ending node
 * @param length amount of nodes including starting- and ending-node
 * @param dir direction the line goes through
 *
 * @author Markus Thielker
 *
 * */
class GomokuFieldConnection(var from : GomokuField, var to : GomokuField, var length : Int, var dir : GomokuFieldDirection) {

    override fun toString() : String {
        return "from: $from -> to: $to with length $length"
    }
}