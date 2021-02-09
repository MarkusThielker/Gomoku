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

    var messagesReceived : Queue<String> = ArrayDeque()
    var connectionOpened = false
    var connectionClosed = false
    var exceptionOccurred = false

    override fun onOpen(handshakedata : ServerHandshake) {
        connectionOpened = true
    }

    override fun onMessage(message : String) {
        messagesReceived.add(message)
    }

    override fun onClose(code : Int, reason : String, remote : Boolean) {
        connectionClosed = true
    }

    override fun onError(ex : Exception) {
        exceptionOccurred = true
    }

    init {
        this.isTcpNoDelay = true
    }
}
