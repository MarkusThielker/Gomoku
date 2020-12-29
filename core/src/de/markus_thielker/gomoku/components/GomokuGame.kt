package de.markus_thielker.gomoku.components

/**
 * The Gomoku class contains the game logic for gomoku.
 *
 * @author Markus Thielker
 *
 * */
class GomokuGame(config : GomokuConfiguration) {

    val board : Array<Array<GomokuField?>> = Array(15) { Array(15) { null } }
    private val listOfLinks = ArrayList<GomokuFieldConnection>()

    private var playerOne : GomokuPlayer = GomokuPlayer(config.playerNameOne, config.playerColorOne)
    private var playerTwo : GomokuPlayer = GomokuPlayer(config.playerNameTwo, config.playerColorTwo)

    private var currentPlayer = playerOne
    private var round = 1

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

            // set clicked field to players color
            board[x][y] = currentPlayer.color

            // evaluate if new stone leads to a winner
            val won : Boolean? = winningCondition(arrayOf(x, y), currentPlayer)

            // evaluate longest line for current player
            val longestLine = longestLine(arrayOf(x, y), currentPlayer)

            // push updates to player instance
            currentPlayer.updateState(arrayOf(x, y), longestLine, won)

            // in case somebody won notify player instances
            won?.let {
                if (currentPlayer == playerOne) playerTwo.updateState(arrayOf(-1, -1), -1, false)
                else playerOne.updateState(arrayOf(-1, -1), -y, false)
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
        currentPlayer = if (currentPlayer == playerOne) playerTwo else playerOne
        round++
    }

    private fun winningCondition(position : Array<Int>, player : GomokuPlayer) : Boolean? {
        // TODO: check for longest line
        return null
    }

    private fun longestLine(position : Array<Int>, player : GomokuPlayer) : Int {
        // TODO: check for longest line
        return 0
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