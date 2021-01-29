package de.markus_thielker.gomoku.components

import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import de.markus_thielker.gomoku.socket.SimpleClient
import de.markus_thielker.gomoku.views.GameView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URI

/**
 * The Gomoku class contains the game logic for gomoku.
 *
 * @author Markus Thielker
 *
 * */
class GomokuGame(
    private val parentView : GameView,
    private val config : GomokuConfiguration,
    var playerOne : GomokuPlayer?,
    var playerTwo : GomokuPlayer?
) {

    val board : Array<Array<GomokuField?>> = Array(15) { Array(15) { null } }
    private val listOfLinks = ArrayList<GomokuFieldConnection>()

    var gameOver = false
    var winnerPlayer : GomokuPlayer

    var currentPlayer : GomokuPlayer
    var round = 1

    init {
        if (playerOne == null) playerOne = GomokuPlayer(config.playerNameOne, GomokuFieldColor.Black)
        if (playerTwo == null) playerTwo = GomokuPlayer(config.playerNameTwo, GomokuFieldColor.White)

        currentPlayer = playerOne!!
        winnerPlayer = playerOne!!
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

        if (board[x][y] == null) {

            // set clicked field to players color
            board[x][y] = GomokuField(arrayOf(x, y), currentPlayer.color)

            // update node  states
            trace(board[x][y]!!, GomokuFieldDirection.Horizontal)
            trace(board[x][y]!!, GomokuFieldDirection.Vertical)
            trace(board[x][y]!!, GomokuFieldDirection.DiagonalTLBR)
            trace(board[x][y]!!, GomokuFieldDirection.DiagonalBLTR)

            board[x][y]!!.printField()

            // evaluate if new stone leads to a winner
            val won : Boolean? = winningCondition()

            // evaluate longest line for current player
            val longestLine = longestLine()

            // push updates to player instance
            currentPlayer.updateState(arrayOf(x, y), longestLine, won)

            // in case somebody won notify player instances
            won?.let {

                // push match result to server
                CoroutineScope(Dispatchers.IO).launch {
                    try {

                        val client = SimpleClient(URI(String.format("ws://%s:%d", "localhost", 42000)))

                        client.connect()

                        delay(500)

                        val playerOneWinner = currentPlayer == playerOne
                        client.pushMatchResult(playerOne!!.name, playerTwo!!.name, playerOneWinner, !playerOneWinner)

                        delay(500)

                        client.closeSession()

                    } catch (exception : Exception) {
                        exception.printStackTrace()
                    }
                }

                gameOver = won
                if (currentPlayer == playerOne) playerTwo!!.updateState(arrayOf(-1, -1), -1, false)
                else playerOne!!.updateState(arrayOf(-1, -1), -y, false)
            }

            switchTurn()

        } else {
            println("At this spot is already a stone placed") // TODO: move warning from console to ui
        }
    }

    /**
     * Evaluates state of neighboring fields for decision of needed connection variant.
     *
     * @param mid new field, being added to board
     * @param dir direction to check on (all directions called parallel)
     *
     * @author Markus Thielker
     *
     * */
    private fun trace(mid : GomokuField, dir : GomokuFieldDirection) {

        val x = mid.pos[0]
        val y = mid.pos[1]

        var oneSkipped = false
        var one : GomokuField? = null

        var twoSkipped = false
        var two : GomokuField? = null

        when (dir) {

            GomokuFieldDirection.Horizontal -> {

                oneSkipped = x <= 0
                if (!oneSkipped) one = board[x - 1][y]

                twoSkipped = x >= 14
                if (!twoSkipped) two = board[x + 1][y]
            }

            GomokuFieldDirection.Vertical -> {

                oneSkipped = y <= 0
                if (!oneSkipped) one = board[x][y - 1]

                twoSkipped = y >= 14
                if (!twoSkipped) two = board[x][y + 1]
            }

            GomokuFieldDirection.DiagonalTLBR -> {

                oneSkipped = x <= 0 || y <= 0
                if (!oneSkipped) one = board[x - 1][y - 1]

                twoSkipped = x >= 14 || y >= 14
                if (!twoSkipped) two = board[x + 1][y + 1]
            }

            GomokuFieldDirection.DiagonalBLTR -> {

                oneSkipped = x >= 14 || y <= 0
                if (!oneSkipped) one = board[x + 1][y - 1]

                twoSkipped = x <= 0 || y >= 14
                if (!twoSkipped) two = board[x - 1][y + 1]
            }
        }

        // check if these fields are valid stones
        val oneValid = one != null && one.color == mid.color && !oneSkipped
        val twoValid = two != null && two.color == mid.color && !twoSkipped

        // if both are valid -> stones placed in middle
        if (oneValid && twoValid) doubleConnection(one, two, dir)
        else {
            if (oneValid) singleConnection(mid, one!!, dir)
            if (twoValid) singleConnection(mid, two!!, dir)
        }
    }

    /**
     * Call if single adaption of field to existing line is needed.
     * Updating field pointers on all ends and setting new length to links.
     *
     * @param mid is the field to be added to the line
     * @param other end-point of line, next to mid-field
     * @param dir direction to determine values while reconnection
     *
     * @author Markus Thielker
     *
     * */
    private fun singleConnection(mid : GomokuField, other : GomokuField, dir : GomokuFieldDirection) {

        val otherLinked = other.con[dir]
        val otherLinkedValid = otherLinked != null && otherLinked.color == other.color

        if (otherLinkedValid) {

            mid.con[dir] = otherLinked
            otherLinked!!.con[dir] = mid

            val temp = mid.conLen[dir]!!
            mid.conLen[dir] = mid.conLen[dir]!! + otherLinked.conLen[dir]!!
            otherLinked.conLen[dir] = otherLinked.conLen[dir]!! + temp

            other.con[dir] = null

            updateList(otherLinked, mid, otherLinked.conLen[dir]!!, dir)

        } else {

            mid.con[dir] = other
            other.con[dir] = mid

            val temp = mid.conLen[dir]!!
            mid.conLen[dir] = mid.conLen[dir]!! + other.conLen[dir]!!
            other.conLen[dir] = other.conLen[dir]!! + temp

            updateList(other, mid, other.conLen[dir]!!, dir)
        }
    }

    /**
     * Call if field is connecting two existing line by being set in the middle.
     * Updating field pointers on all ends and setting new length to links.
     *
     * @param one field next to new set field
     * @param two field next to new set field on opposite side of one
     * @param dir direction to determine values while reconnection
     *
     * @author Markus Thielker
     *
     * */
    private fun doubleConnection(one : GomokuField?, two : GomokuField?, dir : GomokuFieldDirection) {

        // get further connection to the left an pick if valid
        val oneLinked = one!!.con[dir]
        val takeOne = if (oneLinked != null && oneLinked.color == one.color) oneLinked else one

        // get further connection to the right an pick if valid
        val twoLinked = two!!.con[dir]
        val takeTwo = if (twoLinked != null && twoLinked.color == two.color) twoLinked else two

        // connect outer stones to each other
        one.con[dir] = null
        two.con[dir] = null

        // clear previous outer marks
        takeOne.con[dir] = takeTwo
        takeTwo.con[dir] = takeOne

        val temp = takeOne.conLen[dir]!!
        takeOne.conLen[dir] = takeOne.conLen[dir]!! + takeTwo.conLen[dir]!! + 1
        takeTwo.conLen[dir] = takeTwo.conLen[dir]!! + temp + 1

        mergeList(takeOne, takeTwo, takeOne.conLen[dir]!!, dir)
    }

    /**
     * Save or update connections to list to keep overview.
     *
     * @author Markus Thielker
     *
     * */
    private fun updateList(from : GomokuField, to : GomokuField, length : Int, dir : GomokuFieldDirection) {

        listOfLinks.forEach { item ->

            if (item.from == from && item.dir == dir) {
                item.to = to
                item.length = length
                return
            } else if (item.to == from && item.dir == dir) {
                item.from = to
                item.length = length
                return
            }
        }

        listOfLinks.add(GomokuFieldConnection(from, to, length, dir))
    }

    /**
     * Connects two existing connections to one list.
     *
     * @author Markus Thielker
     *
     * */
    private fun mergeList(from : GomokuField, to : GomokuField, length : Int, dir : GomokuFieldDirection) {

        val toRemove = ArrayList<GomokuFieldConnection>()

        var second = false

        listOfLinks.forEach { item ->
            if ((item.from == from || item.to == from) && item.dir == dir) {
                if (!second) {
                    second = true
                    toRemove.add(item)
                } else {
                    toRemove.add(item)
                    return@forEach
                }
            }
        }

        listOfLinks.removeAll(toRemove)

        listOfLinks.add(GomokuFieldConnection(from, to, length, dir))
    }

    /**
     * Call when a players move is completed.
     *
     * @author Markus Thielker
     *
     * */
    private fun switchTurn() {

        when (config.opening) {
            GomokuOpening.Standard -> {

                // switch current player
                switchPlayer()
            }
            GomokuOpening.Swap2 -> {

                // first three stones -> player one | switching colors
                when {
                    round < 3 -> {

                        // switch color of currentPlayer (player one)
                        currentPlayer.color = if (currentPlayer.color == GomokuFieldColor.Black) GomokuFieldColor.White else GomokuFieldColor.Black
                    }
                    round == 3 -> {

                        switchPlayer()

                        val dialog = object : Dialog("How to proceed", parentView.application.skin) {

                            override fun result(result : Any) {

                                parentView.onResultReceived()

                                println("Option: $result")

                                when (result) {
                                    1 -> {
                                        val otherPlayer = if (currentPlayer == playerOne) playerTwo!! else playerOne!!

                                        otherPlayer.color = GomokuFieldColor.Black
                                        currentPlayer.color = GomokuFieldColor.White
                                    }
                                    2 -> {
                                        currentPlayer.color = GomokuFieldColor.Black
                                        switchPlayer()
                                        currentPlayer.color = GomokuFieldColor.White
                                    }
                                    3 -> {
                                        round = 2

                                        val otherPlayer = if (currentPlayer == playerOne) playerTwo!! else playerOne!!
                                        otherPlayer.color = GomokuFieldColor.White
                                    }
                                }
                            }
                        }

                        dialog.button("Play White", 1)
                        dialog.button("Play Black", 2)
                        dialog.button("Opponent decides", 3)

                        parentView.swapTwoDialog(dialog)
                    }
                    round > 3 -> {

                        // switch current player
                        switchPlayer()
                    }
                }
            }
        }

        // increase round count
        round++
    }

    private fun switchPlayer() {
        currentPlayer = if (currentPlayer == playerOne) playerTwo!! else playerOne!!
    }

    /**
     * Checks all existing connections for length of exactly 5 of current players stones to confirm winning condition reached
     *
     * @return true -> Win condition reached | null -> Win condition not reached
     *
     * @author Markus Thielker
     *
     * */
    private fun winningCondition() : Boolean? {
        listOfLinks.forEach { item ->
            if (item.from.color == currentPlayer.color && item.length == 5) {
                winnerPlayer = currentPlayer
                return true
            }
        }
        return null
    }

    /**
     * Checks all existing connections maximum length of current players stones
     *
     * @return value of max. found length
     *
     * @author Markus Thielker
     *
     * */
    private fun longestLine() : Int {
        var max = 0
        listOfLinks.forEach { item -> if (item.from.color == currentPlayer.color && item.length > max) max = item.length }
        return max
    }
}