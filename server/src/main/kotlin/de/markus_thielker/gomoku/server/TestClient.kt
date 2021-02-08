package de.markus_thielker.gomoku.server

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.*


/**
 * This TestClient class is used for the game history servers unit tests.
 * Do not use this WebSocketClient implementation in production.
 *
 * @author Dennis Jehle
 */
class TestClient(server_uri : URI?) : WebSocketClient(server_uri) {

    var messages_received : Queue<String> = ArrayDeque()
    var connection_opened = false
    var connection_closed = false
    var exception_occured = false
    override fun onOpen(handshakedata : ServerHandshake) {
        connection_opened = true
    }

    override fun onMessage(message : String) {
        messages_received.add(message)
    }

    override fun onClose(code : Int, reason : String, remote : Boolean) {
        connection_closed = true
    }

    override fun onError(ex : Exception) {
        exception_occured = true
    }

    init {
        this.isTcpNoDelay = true
    }
}
