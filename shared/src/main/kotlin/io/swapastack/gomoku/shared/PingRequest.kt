package io.swapastack.gomoku.shared

/**
 * The client send this message to the server to receive a PingResponse for calculating the RTT.
 *
 * @param startTime The systems time, when the request object is created
 *
 * @author Markus Thielker
 *
 * */
class PingRequest(var startTime: Long = System.currentTimeMillis()) {

    var messageType = MessageType.PingRequest
}
