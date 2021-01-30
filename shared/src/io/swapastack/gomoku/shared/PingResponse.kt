package io.swapastack.gomoku.shared

import java.sql.Timestamp

class PingResponse(var timestamp : Timestamp) {

    var messageType = MessageType.PingResponse
}