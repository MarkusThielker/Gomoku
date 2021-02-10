package de.markus_thielker.gomoku.socket

import ExtractorMessage
import com.google.gson.Gson
import io.swapastack.gomoku.shared.*
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.*

/**
 * This class was created for testing the simple client. It returns feedback to the client where needed and
 * records it's behavior and events by setting flag variables for later test evaluation.
 *
 * @author Markus Thielker
 *
 * */
class TestServer(address : InetSocketAddress?) : WebSocketServer(address) {

    // flag variables for server actions
    var messagesReceived : Queue<String> = ArrayDeque()
    var connectionOpened = false
    var connectionClosed = false
    var exceptionOccurred = false

    // variable to force timout by not responding
    var forceTimeout = false

    // flag variables for received messages
    var pingRequested = false
    var historySaved = false
    var historyNotSaved = false
    var sessionClosed = false

    private val gson : Gson = Gson()

    private val legalUserIds : ArrayList<UUID> = ArrayList()

    init {
        this.isTcpNoDelay = true
    }

    /** When the server connection is build up, the flag variable is set to true */
    override fun onOpen(conn : WebSocket?, handshake : ClientHandshake?) {
        connectionOpened = true
    }

    /** When the server connection is closed, the flag variable is set to true */
    override fun onClose(conn : WebSocket?, code : Int, reason : String?, remote : Boolean) {
        connectionClosed = true
    }

    /** In this function, incoming messages are handled */
    override fun onMessage(conn : WebSocket?, message : String?) {
        messagesReceived.add(message)

        val extractorMessage : ExtractorMessage = gson.fromJson(message, ExtractorMessage::class.java)
        when (extractorMessage.messageType) {
            MessageType.HelloServer -> {
                val userId = UUID.randomUUID()
                legalUserIds.add(userId)
                val welcomeClient = WelcomeClient(userId, "Moin!")
                val welcomeClientJson : String = gson.toJson(welcomeClient)
                conn!!.send(welcomeClientJson)
            }
            MessageType.PingRequest -> {

                val pingRequest : PingRequest = gson.fromJson(message, PingRequest::class.java)

                pingRequested = true

                // create new HelloServer message object
                val response = PingResponse(pingRequest.timestamp)

                // create JSON String from HelloServer message object
                val responseJSON : String = gson.toJson(response)

                // send JSON encoded HelloServer message as String to the connected WebSocket server
                conn!!.send(responseJSON)

                // 'debug' output
                println("pingResponse sent")
            }
            MessageType.HistoryPush -> {
                val historyPush : HistoryPush = gson.fromJson(message, HistoryPush::class.java)
                if (!legalUserIds.contains(historyPush.userId)) {
                    conn!!.close() // see class description
                }

                if (historyPush.playerOneName == "" || historyPush.playerTwoName == "" || historyPush.playerOneWinner && historyPush.playerTwoWinner) {
                    historyNotSaved = true

                    val historyNotSaved = HistoryNotSaved()
                    val historyNotSavedJson : String = gson.toJson(historyNotSaved)
                    conn!!.send(historyNotSavedJson)
                } else {
                    historySaved = true

                    val historySaved = HistorySaved()
                    val historySavedJson : String = gson.toJson(historySaved)
                    conn!!.send(historySavedJson)
                }
            }
            MessageType.GoodbyeServer -> {
                val goodbyeServer : GoodbyeServer = gson.fromJson(message, GoodbyeServer::class.java)
                if (!legalUserIds.contains(goodbyeServer.userId)) {
                    conn!!.close() // see class description
                }

                sessionClosed = true

                if (!forceTimeout) {
                    val goodbyeClient = GoodbyeClient("Servus!")
                    val goodbyeClientJson : String = gson.toJson(goodbyeClient)
                    conn!!.send(goodbyeClientJson)
                    conn.close() // see network standard
                }
            }
        }
    }

    /** In case that an error occurs, the flag variable is set to true */
    override fun onError(conn : WebSocket?, ex : Exception?) {
        exceptionOccurred = true
    }

    override fun onStart() {}
}