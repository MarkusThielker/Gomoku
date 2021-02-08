package de.markus_thielker.gomoku.socket

import ExtractorMessage
import com.google.gson.Gson
import io.swapastack.gomoku.shared.*
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.*


class TestServer(address : InetSocketAddress?) : WebSocketServer(address) {

    var messagesReceived : Queue<String> = ArrayDeque()
    var connectionOpened = false
    var connectionClosed = false
    var exceptionOccurred = false
    var serverStarted = false

    private val gson : Gson = Gson()

    private val legalUserIds : ArrayList<UUID> = ArrayList()

    private val historyStore : ArrayList<History> = ArrayList()

    init {
        this.isTcpNoDelay = true
    }

    override fun onOpen(conn : WebSocket?, handshake : ClientHandshake?) {
        connectionOpened = true
    }

    override fun onClose(conn : WebSocket?, code : Int, reason : String?, remote : Boolean) {
        connectionClosed = true
    }

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
                val historySaved = HistorySaved()
                val historySavedJson : String = gson.toJson(historySaved)
                conn!!.send(historySavedJson)
            }
            MessageType.GoodbyeServer -> {
                val goodbyeServer : GoodbyeServer = gson.fromJson(message, GoodbyeServer::class.java)
                if (!legalUserIds.contains(goodbyeServer.userId)) {
                    conn!!.close() // see class description
                }
                val goodbyeClient = GoodbyeClient("Servus!")
                val goodbyeClientJson : String = gson.toJson(goodbyeClient)
                conn!!.send(goodbyeClientJson)
                conn.close() // see network standard
            }
        }
    }

    override fun onError(conn : WebSocket?, ex : Exception?) {
        exceptionOccurred = true
    }

    override fun onStart() {
        serverStarted = true
    }
}