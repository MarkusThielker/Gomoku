package io.swapastack.gomoku.shared

/**
 * The server answers with this message, when a ping request is send.
 *
 * @param startTime The time in milliseconds, passed in the PingRequest
 *
 * @author Markus Thielker
 *
 * */
class PingResponse(var startTime: Long) {

    var messageType = MessageType.PingResponse
}
