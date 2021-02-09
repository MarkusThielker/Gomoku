package io.swapastack.gomoku.shared

import java.util.*

/**
 * This class represents the HistoryAll message specified in the network standard.
 * This message is used to send **all** game history entries saved on the server to the client.
 *
 * @author Dennis Jehle
 */
class HistoryAll {

    var messageType = MessageType.HistoryAll
    var history : ArrayList<History> = ArrayList()

    /**
     * This method is used to populate the history ArrayList.
     *
     * @param playerOneName name of player one, not empty, not null
     * @param playerTwoName name of player two, not empty, not null
     * @param playerOneWinner true if player one is the winner
     * @param playerTwoWinner true if player two is the winner
     *
     * @author Dennis Jehle
     */
    fun appendEntry(playerOneName : String, playerTwoName : String, playerOneWinner : Boolean, playerTwoWinner : Boolean) {
        history.add(History(playerOneName, playerTwoName, playerOneWinner, playerTwoWinner))
    }
}