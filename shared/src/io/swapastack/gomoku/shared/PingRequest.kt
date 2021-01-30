package io.swapastack.gomoku.shared

import java.sql.Timestamp

class PingRequest(var timestamp : Timestamp) {

    var messageType = MessageType.PingRequest
}