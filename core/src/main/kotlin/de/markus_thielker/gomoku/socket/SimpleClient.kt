package de.markus_thielker.gomoku.socket

import ExtractorMessage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.swapastack.gomoku.shared.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.sql.Timestamp
import java.util.*

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
class SimpleClient(server_uri : URI?, private val networkController : NetworkController?) : WebSocketClient(server_uri) {

    // 'Google Gson is a java library that can be used to convert Java Object
    // into their JSON representation.'
    // see: https://github.com/google/gson
    // see: https://github.com/google/gson/blob/master/UserGuide.md#TOC-Serializing-and-Deserializing-Generic-Types
    private val gson : Gson = Gson()

    private lateinit var uuid : UUID

    /**
     * This method is called if the connection to the WebSocketServer is open.
     *
     * @param handshake_data [ServerHandshake]
     *
     * @author Markus Thielker
     *
     */
    override fun onOpen(handshake_data : ServerHandshake) {

        // create new HelloServer message object
        val message = HelloServer()

        // create JSON String from HelloServer message object
        val messageJSON : String = gson.toJson(message)

        // send JSON encoded HelloServer message as String to the connected WebSocket server
        send(messageJSON)

        // 'debug' output
        println("new connection opened")
    }

    /**
     * This function sends a pingRequest to the server.
     *
     * @author Markus Thielker
     *
     * */
    fun requestPing() {

        // create new PingRequest message object
        val message = PingRequest(Timestamp(System.currentTimeMillis()))

        // create JSON String from PingRequest message object
        val messageJSON : String = gson.toJson(message)

        // send JSON encoded PingRequest message as String to the connected WebSocket server
        send(messageJSON)

        // 'debug' output
        println("pingRequest sent")
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
        println("closed with exit code $code additional info: $reason")
    }

    /**
     * This message is called if the WebSocketServer sends a String message to the client.
     *
     * @param message a String message from the WebSocketServer e.g. JSON message
     *
     * @author Markus Thielker
     *
     */
    override fun onMessage(message : String) {

        try {

            // extract message
            val extractorMessage : ExtractorMessage = gson.fromJson(message, ExtractorMessage::class.java)

            // differentiate messageTypes
            when (extractorMessage.messageType) {

                // on WelcomeClient -> save userId
                MessageType.WelcomeClient -> {
                    val welcomeClient : WelcomeClient = gson.fromJson(message, WelcomeClient::class.java)
                    uuid = welcomeClient.userId
                }

                // on PingResponse -> notify NetworkController
                MessageType.PingResponse -> {
                    val pingResponse : PingResponse = gson.fromJson(message, PingResponse::class.java)
                    networkController!!.onPingResponse(Timestamp(System.currentTimeMillis()).time - pingResponse.timestamp.time)
                }

                // on HistorySaved -> notify Listener
                MessageType.HistorySaved -> {
                    networkController!!.onHistorySaved()
                }

                // on HistoryNotSaved -> notify Listener
                MessageType.HistoryNotSaved -> {
                    networkController!!.onHistoryNotSaved()
                }

                // on GoodbyeClient -> close connection
                MessageType.GoodbyeClient -> {
                    this.close()
                }
                else -> this.close() // see class description
            }
        } catch (jse : JsonSyntaxException) {
            this.close() // see class description
        }

        // debug received message
        println("received message: $message")
    }

    /**
     * This function is called to close the WebSocketClient session.
     *
     * @author Markus Thielker
     *
     * */
    fun closeSession() {

        // create new GoodbyeServer message object
        val message = GoodbyeServer(uuid)

        // create JSON String from GoodbyeServer Java object
        val messageJSON : String = gson.toJson(message)

        // send JSON encoded GoodbyeServer message as String to the connected WebSocket server
        send(messageJSON)

        // 'debug' output
        println("web socket client session closed")
    }

    /**
     * This function sends the passed match result to the WebSocketServer.
     *
     * @param playerOneName name of player one
     * @param playerTwoName name of player two
     * @param playerOneWinner if player one is the winner
     * @param playerTwoWinner if player two is the winner
     *
     * @author Markus Thielker
     *
     */
    fun pushMatchResult(playerOneName : String, playerTwoName : String, playerOneWinner : Boolean, playerTwoWinner : Boolean) {

        // create new HistoryPush message object
        val message = HistoryPush(uuid, playerOneName, playerTwoName, playerOneWinner, playerTwoWinner)

        // create JSON String from TestMessage Java object
        val messageJSON : String = gson.toJson(message)

        // send JSON encoded HistoryPush message as String to the connected WebSocket server
        send(messageJSON)

        // 'debug' output
        println("match result pushed")
    }

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
