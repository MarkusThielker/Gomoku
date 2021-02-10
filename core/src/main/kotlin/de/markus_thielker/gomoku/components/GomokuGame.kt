package de.markus_thielker.gomoku.components

import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import de.markus_thielker.gomoku.socket.NetworkController
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
    private val opening : GomokuOpening,
    var playerOne : GomokuPlayer,
    var playerTwo : GomokuPlayer
) : NetworkController {

    // two-dimensional array for stones with default value null
    val board : Array<Array<GomokuField?>> = Array(15) { Array(15) { null } }

    // array list containing all connections with length > 2
    private val listOfLinks = ArrayList<GomokuFieldConnection>()

    var gameOver = false
    var winnerPlayer : GomokuPlayer? = null

    var currentPlayer = playerOne
    var round = 1

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

        // check if position is empty
        if (board[x][y] == null) {

            // set clicked field to players color
            board[x][y] = GomokuField(arrayOf(x, y), currentPlayer.color)

            // update node states
            trace(board[x][y]!!, GomokuFieldDirection.Horizontal)
            trace(board[x][y]!!, GomokuFieldDirection.Vertical)
            trace(board[x][y]!!, GomokuFieldDirection.DiagonalTLBR)
            trace(board[x][y]!!, GomokuFieldDirection.DiagonalBLTR)

            // print field information
            board[x][y]!!.printField()

            // evaluate if new stone leads to a winner
            val won : Boolean? = winningCondition()

            // evaluate longest line for current player
            val longestLine = longestLine()

            // push updates to player instance
            currentPlayer.updateState(arrayOf(x, y), longestLine, won)

            // in case somebody won notify player instances
            won?.let {

                // push match result to server : coroutine to not block main thread
                CoroutineScope(Dispatchers.IO).launch {
                    try {

                        // create client socket focusing on local server
                        val client = SimpleClient(URI(String.format("ws://%s:%d", "localhost", 42000)), this@GomokuGame)

                        // connect to passed connection
                        client.connect()

                        // timeout to ensure connection
                        delay(500)

                        // get winner and push to server
                        val playerOneWinner = currentPlayer == playerOne
                        client.pushMatchResult(playerOne.name, playerTwo.name, playerOneWinner, !playerOneWinner)

                        // timeout to ensure connection
                        delay(500)

                        // close connection by sending goodbye to server
                        client.closeSession()

                    } catch (exception : Exception) {
                        exception.printStackTrace()
                    }
                }

                // update game view
                gameOver = won

                // update player objects and
                if (currentPlayer == playerOne) playerTwo.updateState(arrayOf(-1, -1), -1, false)
                else playerOne.updateState(arrayOf(-1, -1), -y, false)
            }

            // check for tie
            if ((playerOne.placed + playerTwo.placed) == parentView.gridSize * parentView.gridSize) gameOver = true

            // switch turn
            switchTurn()

        } else {

            // show message for blocked field
            parentView.sendMessage("Fehler", "Auf diesem Feld liegt bereits ein Stein")
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

        // get field coordinates
        val x = mid.pos[0]
        val y = mid.pos[1]

        // declare values for further process
        var oneSkipped = false
        var one : GomokuField? = null

        var twoSkipped = false
        var two : GomokuField? = null

        // check for validity for neighbored fields
        when (dir) {

            // check for horizontal neighbors
            GomokuFieldDirection.Horizontal -> {

                oneSkipped = x <= 0
                if (!oneSkipped) one = board[x - 1][y]

                twoSkipped = x >= 14
                if (!twoSkipped) two = board[x + 1][y]
            }

            // check for vertical neighbors
            GomokuFieldDirection.Vertical -> {

                oneSkipped = y <= 0
                if (!oneSkipped) one = board[x][y - 1]

                twoSkipped = y >= 14
                if (!twoSkipped) two = board[x][y + 1]
            }

            // check for diagonal neighbors (left-above and right-below)
            GomokuFieldDirection.DiagonalTLBR -> {

                oneSkipped = x <= 0 || y <= 0
                if (!oneSkipped) one = board[x - 1][y - 1]

                twoSkipped = x >= 14 || y >= 14
                if (!twoSkipped) two = board[x + 1][y + 1]
            }

            // check for diagonal neighbors (right-above and left-below)
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
        // else placed at the end of a connection
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

        // search for connection already in list
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

        // line only reached if connection not in list -> add connection
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

        // differentiate for opening rules
        when (opening) {

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

                        // switch player to indicate who is selecting opening procedure
                        switchPlayer()

                        // create dialog with result callback
                        val dialog = object : Dialog("Eröffnungsregel", parentView.application.skin) {

                            // execute on result selected
                            override fun result(result : Any) {

                                // notify UI for result received
                                parentView.onResultReceived()

                                // differentiate selections
                                when (result) {

                                    // on "play white"
                                    1 -> {
                                        val otherPlayer = if (currentPlayer == playerOne) playerTwo else playerOne

                                        otherPlayer.color = GomokuFieldColor.Black
                                        currentPlayer.color = GomokuFieldColor.White
                                    }

                                    // on "play black"
                                    2 -> {
                                        currentPlayer.color = GomokuFieldColor.Black
                                        switchPlayer()
                                        currentPlayer.color = GomokuFieldColor.White
                                    }

                                    // on "opponent decides"
                                    3 -> {
                                        round = 2
                                        val otherPlayer = if (currentPlayer == playerOne) playerTwo else playerOne
                                        otherPlayer.color = GomokuFieldColor.White
                                    }
                                }
                            }
                        }

                        // add buttons to dialog
                        val btnSelectionOne = TextButton("Weiße Steine spielen", parentView.application.skin).apply {
                            label.style = parentView.generateLabelStyle("Weiße Steine spielen")
                        }
                        dialog.button(btnSelectionOne, 1)
                        val btnSelectionTwo = TextButton("Schwarze Steine spielen", parentView.application.skin).apply {
                            label.style = parentView.generateLabelStyle("Schwarze Steine spielen")
                        }
                        dialog.button(btnSelectionTwo, 2)
                        val btnSelectionThree = TextButton("Gegner entscheidet", parentView.application.skin).apply {
                            label.style = parentView.generateLabelStyle("Gegner entscheidet")
                        }
                        dialog.button(btnSelectionThree, 3)

                        // show dialog on UI
                        parentView.showDialog(dialog)
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

    /**
     * This function changes the currentPlayer to the player, not being the currentPlayer
     *
     * @author Markus Thielker
     *
     * */
    private fun switchPlayer() {
        currentPlayer = if (currentPlayer == playerOne) playerTwo else playerOne
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

    override fun onPingResponse(ping : Long) {
        // no pinging from this view
    }

    /**
     * This function is called when the pushed history was saved.
     *
     * @author Markus Thielker
     *
     * */
    override fun onHistorySaved() {
        parentView.sendMessage("Gespeichert", "Spielergebnis auf Server gespeichert")
    }

    /**
     * This function is called when the pushed history was not saved.
     *
     * @author Markus Thielker
     *
     * */
    override fun onHistoryNotSaved() {
        parentView.sendMessage("Fehler", "Das Spielergebnis konnte nicht gespeichert werden")
    }
}