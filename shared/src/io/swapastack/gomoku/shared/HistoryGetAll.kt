package io.swapastack.gomoku.shared

import java.util.*

/**
 * This class represents the HistoryGetAll message specified in the network standard.
 * This message is used to request the **complete** game server history.
 * This message must contain the connection specific userId as UUID.
 *
 * @author Dennis Jehle
 */
class HistoryGetAll(var userId : UUID) {

    var messageType = MessageType.HistoryGetAll
}