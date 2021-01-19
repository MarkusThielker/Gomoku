package io.swapastack.gomoku.shared

import java.util.*

/**
 * This class represents the HistoryPush message specified in the network standard.
 * This message is used to add a new game history record to the game history server.
 * This message must contain the connection specific userId as UUID.
 * This message must contain two non-empty && non-null Strings for playerOneName, playerTwoName.
 * This message must contain two boolean values for playerOneWinner, playerTwoWinner, they must not be both true.
 *
 * @author Dennis Jehle
 */
class HistoryPush(var userId : UUID, var playerOneName : String, var playerTwoName : String, var playerOneWinner : Boolean, var playerTwoWinner : Boolean) {

    var messageType = MessageType.HistoryPush
}