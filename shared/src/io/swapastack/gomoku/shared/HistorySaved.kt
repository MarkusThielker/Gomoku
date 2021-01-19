package io.swapastack.gomoku.shared

/**
 * This class represents the HistorySaved message specified in the network standard.
 * This message is used to confirm that the game history was saved.
 * This message can happen after the server received a HistoryPush message from the client.
 *
 * @author Dennis Jehle
 */
class HistorySaved {

    var messageType = MessageType.HistorySaved
}