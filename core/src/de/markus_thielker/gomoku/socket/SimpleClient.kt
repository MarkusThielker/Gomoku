package de.markus_thielker.gomoku.socket


import com.google.gson.Gson
import io.swapastack.gomoku.shared.HelloServer
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer


/**
 * The SimpleClient extends the WebSocketClient class.
 * The SimpleClient class could be used to establish a WebSocket (ws, wss) connection
 * a WebSocketServer.
 *
 * The send(String message) method could be used to send a String message to the
 * WebSocketServer.
 *
 * note: this client could be used to implement the network standard document.
 *
 * @author Dennis Jehle
 */
class SimpleClient(server_uri : URI?) : WebSocketClient(server_uri) {

    // 'Google Gson is a java library that can be used to convert Java Object
    // into their JSON representation.'
    // see: https://github.com/google/gson
    // see: https://github.com/google/gson/blob/master/UserGuide.md#TOC-Serializing-and-Deserializing-Generic-Types
    private val gson : Gson = Gson()

    /**
     * This method is called if the connection to the WebSocketServer is open.
     *
     * @param handshake_data [ServerHandshake]
     * @author Dennis Jehle
     */
    override fun onOpen(handshake_data : ServerHandshake) {
        // create new TestMassage Java object
        val message = HelloServer()
        // create JSON String from TestMessage Java object
        val testMessage : String = gson.toJson(message)
        // send JSON encoded test message as String to the connected WebSocket server
        send(testMessage)
        // 'debug' output
        println("new connection opened")
    }

    /**
     * This method is called if the connection to the WebSocketServer was closed.
     *
     * @param code status code
     * @param reason the reason for closing the connection
     * @param remote was the close initiated by the remote host
     * @author Dennis Jehle
     */
    override fun onClose(code : Int, reason : String, remote : Boolean) {
        // TODO: implement client sign-off
        println("closed with exit code $code additional info: $reason")
    }

    /**
     * This message is called if the WebSocketServer sends a String message to the client.
     *
     * @param message a String message from the WebSocketServer e.g. JSON message
     * @author Dennis Jehle
     */
    override fun onMessage(message : String) {
        // TODO: parse message type and content
        println("received message: $message")
    }

    // TODO: add history push function

    // TODO: add history pull function

    /**
     * This method is called if the WebSocketServer send a binary message to the client.
     * note: This method is not necessary for this project, because the network standard
     * document specifies a JSON String message protocol.
     *
     * @param message a binary message
     * @author Dennis Jehle
     */
    override fun onMessage(message : ByteBuffer) {
        // do nothing, because binary messages are not supported
    }

    /**
     * This method is called if an exception was thrown.
     *
     * @param exception [Exception]
     * @author Dennis Jehle
     */
    override fun onError(exception : Exception) {
        System.err.println("an error occurred:$exception")
    }
}
