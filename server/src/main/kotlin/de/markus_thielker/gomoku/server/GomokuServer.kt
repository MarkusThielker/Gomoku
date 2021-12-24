package de.markus_thielker.gomoku.server

import ExtractorMessage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.swapastack.gomoku.shared.*
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.*


/**
 * This is the reference implementation of the gomoku game history server.
 * Note:
 * 1. This server implements the network standard
 * 2. This server is super strict with not supported / malformed messages (except HistoryPush).
 * If a message is not supported or malformed the connection to the client gets closed.
 * 3. There are jUnit unit tests for this server located at /gomoku/server/test/
 * 4. Feel free to harden the implementation of this reference server if you find some edge
 * cases that are not or not correctly handled.
 * 5. This server only does a simple validation of the userId UUId. The server generates a UUID
 * after receiving the HelloServer message and stores it. If the client sends further messages
 * the UUID in the message is checked against a List with valid UUIDs. So the validation is not
 * that secure, feel free to improve this.
 * 6. The server does not save the history to disc, so if you want to clear the history you just
 * have to restart the server. The main reason for this is "testing" while developing the client.
 * If you want to add the saving functionality to this server, then feel free, there is a TODO
 * in the code.
 *
 * @author Dennis Jehle, converted to kotlin by Markus Thielker
 *
 *
 */
class GomokuServer(address : InetSocketAddress?) : WebSocketServer(address) {

    // 'Google Gson is a java library that can be used to convert Java Object
    // into their JSON representation.'
    // see: https://github.com/google/gson
    // see: https://github.com/google/gson/blob/master/UserGuide.md#TOC-Serializing-and-Deserializing-Generic-Types
    private val gson : Gson = Gson()

    // This ArrayList stores the UUIDs generated after receiving the HelloServer message.
    // They are used to "validate" if the messages sent by a connected client are legal.
    private val legalUserIds : ArrayList<UUID> = ArrayList()

    // The History store
    private val historyStore : ArrayList<History> = ArrayList()

    /**
     * This method is called if a new client connected.
     *
     * @param conn [WebSocket]
     * @param handshake [ClientHandshake]
     * @author Dennis Jehle
     */
    override fun onOpen(conn : WebSocket, handshake : ClientHandshake?) {
        // 'debug' output
        println("new connection to " + conn.remoteSocketAddress)
    }

    /**
     * This method is called if a WebSocket connection was closed.
     *
     * @param conn [WebSocket]
     * @param code status code
     * @param reason String containing closing reason
     * @param remote close was initiated by remote client
     * @author Dennis Jehle
     */
    override fun onClose(conn : WebSocket, code : Int, reason : String, remote : Boolean) {
        println("closed " + conn.remoteSocketAddress.toString() + " with exit code " + code.toString() + " additional info: " + reason)
    }

    /**
     * This method is called if a String message was received.
     * e.g. connected client sends a JSON encoded String message
     *
     * @param conn [WebSocket]
     * @param message the String message, e.g. JSON String
     * @author Dennis Jehle
     */
    override fun onMessage(conn : WebSocket, message : String?) {
        try {
            val extractorMessage : ExtractorMessage = gson.fromJson(message, ExtractorMessage::class.java)
            when (extractorMessage.messageType) {
                MessageType.HelloServer -> {
                    val userId = UUID.randomUUID()
                    legalUserIds.add(userId)
                    val welcomeClient = WelcomeClient(userId, "Moin!")
                    val welcomeClientJson : String = gson.toJson(welcomeClient)
                    conn.send(welcomeClientJson)
                }
                MessageType.PingRequest -> {

                    val pingRequest : PingRequest = gson.fromJson(message, PingRequest::class.java)

                    // create new PingResponse message object
                    val response = PingResponse(pingRequest.startTime)

                    // create JSON String from PingResponse message object
                    val responseJSON : String = gson.toJson(response)

                    // send JSON encoded PingResponse message as String to the connected WebSocket server
                    conn.send(responseJSON)

                    // 'debug' output
                    println("pingResponse sent")
                }
                MessageType.HistoryPush -> {
                    val historyPush : HistoryPush = gson.fromJson(message, HistoryPush::class.java)
                    if (!legalUserIds.contains(historyPush.userId)) {
                        conn.close() // see class description
                    }
                    val playerOneName : String = historyPush.playerOneName
                    val playerTwoName : String = historyPush.playerTwoName
                    val playerOneWinner : Boolean = historyPush.playerOneWinner
                    val playerTwoWinner : Boolean = historyPush.playerTwoWinner
                    if (playerOneName == "" || playerTwoName == "" || playerOneWinner && playerTwoWinner) {
                        val historyNotSaved = HistoryNotSaved()
                        val historyNotSavedJson : String = gson.toJson(historyNotSaved)
                        conn.send(historyNotSavedJson)
                    } else {
                        historyStore.add(History(playerOneName, playerTwoName, playerOneWinner, playerTwoWinner))
                        // TODO: here the history should be actually saved to disc
                        val historySaved = HistorySaved()
                        val historySavedJson : String = gson.toJson(historySaved)
                        conn.send(historySavedJson)
                    }
                }
                MessageType.HistoryGetAll -> {
                    val historyGetAll : HistoryGetAll = gson.fromJson(message, HistoryGetAll::class.java)
                    if (!legalUserIds.contains(historyGetAll.userId)) {
                        conn.close() // see class description
                    }
                    val historyAll = HistoryAll()
                    for (h in historyStore) {
                        historyAll.appendEntry(h.playerOneName, h.playerTwoName, h.playerOneWinner, h.playerTwoWinner)
                    }
                    val historyAllJson : String = gson.toJson(historyAll)
                    conn.send(historyAllJson)
                }
                MessageType.GoodbyeServer -> {
                    val goodbyeServer : GoodbyeServer = gson.fromJson(message, GoodbyeServer::class.java)
                    if (!legalUserIds.contains(goodbyeServer.userId)) {
                        conn.close() // see class description
                    }
                    val goodbyeClient = GoodbyeClient("Servus!")
                    val goodbyeClientJson : String = gson.toJson(goodbyeClient)
                    conn.send(goodbyeClientJson)
                    conn.close() // see network standard
                }
                else -> conn.close() // see class description
            }
        } catch (jse : JsonSyntaxException) {
            conn.close() // see class description
        }
    }

    /**
     * This method is called if a binary message was received.
     * note: this method is not necessary for this project, because
     * the network standard document specifies a JSON String message protocol
     *
     * @param conn [WebSocket]
     * @param message the binary message
     * @author Dennis Jehle
     */
    override fun onMessage(conn : WebSocket?, message : ByteBuffer?) {
        // do nothing, because binary messages are not supported
    }

    /**
     * This method is called if an exception was thrown.
     *
     * @param conn [WebSocket]
     * @param exception [Exception]
     * @author Dennis Jehle
     */
    override fun onError(conn : WebSocket?, exception : Exception) {
        System.err.println("an error occurred on connection " + conn?.remoteSocketAddress.toString() + ":" + exception)
    }

    /**
     * This method is called if the server started successfully.
     *
     * @author Dennis Jehle
     */
    override fun onStart() {
        println("Gomoku server started successfully.")
    }

    companion object {

        // hostname / ip to bind
        // e.g. localhost
        // e.g. 127.0.0.1
        private const val host = "localhost"

        // port to listen on
        // see: https://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers
        private const val port = 42000

        /**
         * GomokuServer main method.
         *
         * @param args command line arguments
         * @author Dennis Jehle
         */
        @JvmStatic
        fun main(args : Array<String>) {
            // create WebSocketServer
            val server : WebSocketServer = GomokuServer(InetSocketAddress(host, port))
            // see: https://github.com/TooTallNate/Java-WebSocket/wiki/Enable-SO_REUSEADDR
            server.isReuseAddr = true
            // see: https://github.com/TooTallNate/Java-WebSocket/wiki/Enable-TCP_NODELAY
            server.isTcpNoDelay = true
            // start the WebSocketServer
            server.start()

            // create ShutdownHook to catch CTRL+C and shutdown server peacefully
            // see: https://docs.oracle.com/javase/8/docs/technotes/guides/lang/hook-design.html
            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    try {
                        println("ShutdownHook executed.")
                        sleep(500)
                        println("Application shutting down.")
                        // shutdown server
                        server.stop()
                    } catch (ie : InterruptedException) {
                        System.out.printf("InterruptedException: %s", ie)
                        currentThread().interrupt()
                    } catch (ioe : IOException) {
                        System.out.printf("IOException: %s", ioe)
                    }
                }
            })
        }
    }
}
