package de.markus_thielker.gomoku.components

/**
 * The Gomoku class contains the game logic for gomoku.
 *
 * @author Markus Thielker
 *
 * */
class GomokuGame(config : GomokuConfiguration) {

    private val board = Array(15) { Array(15) { GomokuField.None } }

    private var roundCounter = 1
    private var playerOneTurn = true

    private var playerOne : GomokuPlayer = GomokuPlayer(config.playerNameOne, config.playerColorOne)
    private var playerTwo : GomokuPlayer = GomokuPlayer(config.playerNameTwo, config.playerColorTwo)

    // TODO: remove debugging function
    init {
        printBoard()
    }

    /**
     * Call when a stone is placed. Checks if passed position is valid and if true updates
     * the players state, switch player being next.
     *
     * @param x x-coordinate of the stone placed (0-14)
     * @param y y-coordinate of the stone placed (0-14)
     *
     * @author Markus Thielker
     *
     * */
    fun stonePlaced(x : Int, y : Int) {

        // TODO: integrate opening rules

        if (board[x][y] == GomokuField.None) {

            if (playerOneTurn) {
                playerOne.updateState(arrayOf(x, y), 1, null)
                board[x][y] = playerOne.color
            } else {
                playerTwo.updateState(arrayOf(x, y), 1, null)
                board[x][y] = playerTwo.color
            }

            printBoard() // TODO: remove debugging call
            switchTurn()

        } else {
            // TODO: show feedback in user interface
            println("At this spot is already a stone placed") // TODO: remove debugging call
        }
    }

    /**
     * Call when a players move is completed.
     *
     * @author Markus Thielker
     *
     * */
    private fun switchTurn() {
        playerOneTurn = !playerOneTurn
        roundCounter++
    }

    private fun winningCondition(player : GomokuPlayer) {
        // TODO: check for winning condition
    }

    private fun longestLine(player : GomokuPlayer) {
        // TODO: check for longest line
    }

    // TODO: remove debugging function
    private fun printBoard() {
        println("\n\nboard display test:")
        board.forEach { line -> line.forEach { field -> print("$field, ") }; println() }

        println()

        println(playerOne.toString())
        println(playerTwo.toString())
    }
}