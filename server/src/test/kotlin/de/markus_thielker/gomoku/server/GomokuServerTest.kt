package de.markus_thielker.gomoku.server

import com.google.gson.Gson
import io.swapastack.gomoku.shared.*
import org.java_websocket.server.WebSocketServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.InetSocketAddress
import java.net.URI
import java.net.URISyntaxException
import java.sql.Timestamp

class GomokuServerTest {

    @Test
    fun alwaysTrue() {
        assert(true)
    }

    /**
     * This test is used to check if the WebSocket connection could be established between client and server.
     * Since the WebSocket framework uses non-blocking methods, there is a Thread.sleep() call in this test method.
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     *
     * @author Dennis Jehle, converted to kotlin by Markus Thielker
     */
    @Test
    @Throws(IOException::class, InterruptedException::class, URISyntaxException::class)
    fun startServerAndConnect() {
        val hostname = "localhost"
        val port = 42001
        val serverUri = URI(String.format("ws://%s:%d", hostname, port))
        val server : WebSocketServer = GomokuServer(InetSocketAddress(hostname, port))
        server.isReuseAddr = true
        server.isTcpNoDelay = true
        server.start()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        val testClient = TestClient(serverUri)
        testClient.connect()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assertEquals(true, testClient.connectionOpened)
        testClient.close()
        server.stop()
    }

    /**
     * This test is used to check if the server send a WelcomeClient message after receiving the HelloServer
     * message as specified in the network standard.
     * This test also checks if the UUID is not null and the welcome message is "Moin!".
     *
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     *
     * @author Dennis Jehle, converted to kotlin by Markus Thielker
     */
    @Test
    @Throws(URISyntaxException::class, InterruptedException::class, IOException::class)
    fun sendHelloServerMessage() {
        val gson = Gson()
        val hostname = "localhost"
        val port = 42002
        val serverUri = URI(String.format("ws://%s:%d", hostname, port))
        val server : WebSocketServer = GomokuServer(InetSocketAddress(hostname, port))
        server.isReuseAddr = true
        server.isTcpNoDelay = true
        server.start()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        val testClient = TestClient(serverUri)
        testClient.connect()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assertEquals(true, testClient.connectionOpened)
        val helloServerJson = "{messageType:\"HelloServer\"}"
        testClient.send(helloServerJson)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assert(0 != testClient.messagesReceived.size)
        val answer : String = testClient.messagesReceived.poll()
        val welcomeClient = gson.fromJson(answer, WelcomeClient::class.java)
        assert(null != welcomeClient.userId)
        assert(welcomeClient.welcomeMessage == "Moin!")
        testClient.close()
        server.stop()
    }

    /**
     * This test is used to check if the server send a PingResponse message after receiving the PingRequest
     * This test also checks if the initial timestamp gets returned as well.
     *
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     *
     * @author Markus Thielker
     */
    @Test
    @Throws(URISyntaxException::class, InterruptedException::class, IOException::class)
    fun sendPingRequest() {
        val gson = Gson()
        val hostname = "localhost"
        val port = 42002
        val serverUri = URI(String.format("ws://%s:%d", hostname, port))
        val server : WebSocketServer = GomokuServer(InetSocketAddress(hostname, port))
        server.isReuseAddr = true
        server.isTcpNoDelay = true
        server.start()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        val testClient = TestClient(serverUri)
        testClient.connect()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assertEquals(true, testClient.connectionOpened)
        val pingRequestJson = gson.toJson(PingRequest())
        testClient.send(pingRequestJson)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assert(0 != testClient.messagesReceived.size)
        val answer : String = testClient.messagesReceived.poll()
        val pingResponse = gson.fromJson(answer, PingResponse::class.java)
        assert(pingResponse.startTime > 0)
        testClient.close()
        server.stop()
    }

    /**
     * This test is used to check if the server handles a valid HistoryPush message correctly.
     *
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     *
     * @author Dennis Jehle, converted to kotlin by Markus Thielker
     */
    @Test
    @Throws(URISyntaxException::class, InterruptedException::class, IOException::class)
    fun sendValidHistoryPushMessage() {
        val gson = Gson()
        val hostname = "localhost"
        val port = 42003
        val serverUri = URI(String.format("ws://%s:%d", hostname, port))
        val server : WebSocketServer = GomokuServer(InetSocketAddress(hostname, port))
        server.isReuseAddr = true
        server.isTcpNoDelay = true
        server.start()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        val testClient = TestClient(serverUri)
        testClient.connect()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assertEquals(true, testClient.connectionOpened)
        val helloServerJson = "{messageType:\"HelloServer\"}"
        testClient.send(helloServerJson)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assert(1 == testClient.messagesReceived.size)
        val answer : String = testClient.messagesReceived.poll()
        val welcomeClient = gson.fromJson(answer, WelcomeClient::class.java)
        val userId = welcomeClient.userId
        val historyPush = HistoryPush(userId, "Hubert", "Helga", playerOneWinner = false, playerTwoWinner = true)
        val historyPushJson = gson.toJson(historyPush)
        testClient.send(historyPushJson)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assert(1 == testClient.messagesReceived.size)
        val historySaved : HistorySaved = gson.fromJson(testClient.messagesReceived.poll(), HistorySaved::class.java)
        assert(historySaved.messageType === MessageType.HistorySaved)
        testClient.close()
        server.stop()
    }

    /**
     * This test is used to check if the server handles a invalid HistoryPush message correctly.
     *
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     *
     * @author Dennis Jehle, converted to kotlin by Markus Thielker
     */
    @Test
    @Throws(URISyntaxException::class, InterruptedException::class, IOException::class)
    fun sendInvalidHistoryPushMessage() {
        val gson = Gson()
        val hostname = "localhost"
        val port = 42004
        val serverUri = URI(String.format("ws://%s:%d", hostname, port))
        val server : WebSocketServer = GomokuServer(InetSocketAddress(hostname, port))
        server.isReuseAddr = true
        server.isTcpNoDelay = true
        server.start()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        val testClient = TestClient(serverUri)
        testClient.connect()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assertEquals(true, testClient.connectionOpened)
        val helloServerJson = "{messageType:\"HelloServer\"}"
        testClient.send(helloServerJson)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assert(1 == testClient.messagesReceived.size)
        val answer : String = testClient.messagesReceived.poll()
        val welcomeClient = gson.fromJson(answer, WelcomeClient::class.java)
        assert(null != welcomeClient.userId)
        val userId = welcomeClient.userId

        // invalid
        val historyPush = HistoryPush(userId, "Hubert", "Helga", playerOneWinner = true, playerTwoWinner = true)
        val historyPushJson = gson.toJson(historyPush)
        testClient.send(historyPushJson)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assert(1 == testClient.messagesReceived.size)
        val historyNotSaved : HistoryNotSaved = gson.fromJson(testClient.messagesReceived.poll(), HistoryNotSaved::class.java)
        assert(historyNotSaved.messageType === MessageType.HistoryNotSaved)
        testClient.close()
        server.stop()
    }

    /**
     * This test is used to check that the client can send more than one HistoryPush message to the server.
     * This is specified in the network standard.
     *
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     *
     * @author Dennis Jehle, converted to kotlin by Markus Thielker
     */
    @Test
    @Throws(URISyntaxException::class, InterruptedException::class, IOException::class)
    fun sendTwoHistoryPushMessages() {
        val gson = Gson()
        val hostname = "localhost"
        val port = 42005
        val serverUri = URI(String.format("ws://%s:%d", hostname, port))
        val server : WebSocketServer = GomokuServer(InetSocketAddress(hostname, port))
        server.isReuseAddr = true
        server.isTcpNoDelay = true
        server.start()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        val testClient = TestClient(serverUri)
        testClient.connect()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assertEquals(true, testClient.connectionOpened)
        val helloServerJson = "{messageType:\"HelloServer\"}"
        testClient.send(helloServerJson)
        Thread.sleep(500)
        assert(1 == testClient.messagesReceived.size)
        val answer : String = testClient.messagesReceived.poll()
        val welcomeClient = gson.fromJson(answer, WelcomeClient::class.java)
        assert(null != welcomeClient.userId)
        val userId = welcomeClient.userId
        val historyPush1 = HistoryPush(userId, "Hubert", "Helga", playerOneWinner = true, playerTwoWinner = false)
        val historyPush1Json = gson.toJson(historyPush1)
        testClient.send(historyPush1Json)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        val historyPush2 = HistoryPush(userId, "Anton", "Anna", playerOneWinner = false, playerTwoWinner = true)
        val historyPush2Json = gson.toJson(historyPush2)
        testClient.send(historyPush2Json)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assert(2 == testClient.messagesReceived.size)
        val history1Saved : HistorySaved = gson.fromJson(testClient.messagesReceived.poll(), HistorySaved::class.java)
        assert(history1Saved.messageType === MessageType.HistorySaved)
        val history2Saved : HistorySaved = gson.fromJson(testClient.messagesReceived.poll(), HistorySaved::class.java)
        assert(history2Saved.messageType === MessageType.HistorySaved)
        testClient.close()
        server.stop()
    }

    /**
     * This test is used to check that the HistoryGetAll message is handled correctly within the server.
     * The test sends two valid HistoryPush messages to the server and a HistoryGetAll message.
     * So the HistoryAll message should contain the two History entries.
     *
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     *
     * @author Dennis Jehle, converted to kotlin by Markus Thielker
     */
    @Test
    @Throws(URISyntaxException::class, InterruptedException::class, IOException::class)
    fun sendTwoHistoryPushMessagesAndGetAll() {
        val gson = Gson()
        val hostname = "localhost"
        val port = 42006
        val serverUri = URI(String.format("ws://%s:%d", hostname, port))
        val server : WebSocketServer = GomokuServer(InetSocketAddress(hostname, port))
        server.isReuseAddr = true
        server.isTcpNoDelay = true
        server.start()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        val testClient = TestClient(serverUri)
        testClient.connect()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assertEquals(true, testClient.connectionOpened)
        val helloServerJson = "{messageType:\"HelloServer\"}"
        testClient.send(helloServerJson)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assert(1 == testClient.messagesReceived.size)
        val answer : String = testClient.messagesReceived.poll()
        val welcomeClient = gson.fromJson(answer, WelcomeClient::class.java)
        assert(null != welcomeClient.userId)
        val userId = welcomeClient.userId
        val historyPush1 = HistoryPush(userId, "Hubert", "Helga", playerOneWinner = true, playerTwoWinner = false)
        val historyPush1Json = gson.toJson(historyPush1)
        testClient.send(historyPush1Json)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        val historyPush2 = HistoryPush(userId, "Anton", "Anna", playerOneWinner = false, playerTwoWinner = true)
        val historyPush2Json = gson.toJson(historyPush2)
        testClient.send(historyPush2Json)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assert(2 == testClient.messagesReceived.size)
        val history1Saved : HistorySaved = gson.fromJson(testClient.messagesReceived.poll(), HistorySaved::class.java)
        assert(history1Saved.messageType === MessageType.HistorySaved)
        val history2Saved : HistorySaved = gson.fromJson(testClient.messagesReceived.poll(), HistorySaved::class.java)
        assert(history2Saved.messageType === MessageType.HistorySaved)
        val historyGetAll = HistoryGetAll(userId)
        val historyGetAllJson = gson.toJson(historyGetAll)
        testClient.send(historyGetAllJson)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assert(1 == testClient.messagesReceived.size)
        val historyAll : HistoryAll = gson.fromJson(testClient.messagesReceived.poll(), HistoryAll::class.java)
        assert(historyAll.history[0].playerOneName == "Hubert")
        assert(historyAll.history[0].playerTwoName == "Helga")
        assert(historyAll.history[0].playerOneWinner)
        assert(!historyAll.history[0].playerTwoWinner)
        assert(historyAll.history[1].playerOneName == "Anton")
        assert(historyAll.history[1].playerTwoName == "Anna")
        assert(!historyAll.history[1].playerOneWinner)
        assert(historyAll.history[1].playerTwoWinner)
        testClient.close()
        server.stop()
    }

    /**
     * This test is used to check if the server handles the GoodbyeServer message correctly.
     *
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     *
     * @author Dennis Jehle, converted to kotlin by Markus Thielker
     */
    @Test
    @Throws(URISyntaxException::class, InterruptedException::class, IOException::class)
    fun sendGoodbyeServerMessage() {
        val gson = Gson()
        val hostname = "localhost"
        val port = 42007
        val serverUri = URI(String.format("ws://%s:%d", hostname, port))
        val server : WebSocketServer = GomokuServer(InetSocketAddress(hostname, port))
        server.isReuseAddr = true
        server.isTcpNoDelay = true
        server.start()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        val testClient = TestClient(serverUri)
        testClient.connect()
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assertEquals(true, testClient.connectionOpened)
        val helloServerJson = "{messageType:\"HelloServer\"}"
        testClient.send(helloServerJson)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assert(1 == testClient.messagesReceived.size)
        val answer : String = testClient.messagesReceived.poll()
        val welcomeClient = gson.fromJson(answer, WelcomeClient::class.java)
        assert(null != welcomeClient.userId)
        val userId = welcomeClient.userId
        val goodbyeServer = GoodbyeServer(userId)
        val goodbyeServerJson = gson.toJson(goodbyeServer)
        testClient.send(goodbyeServerJson)
        Thread.sleep(DELAY_TIME_MILLISECONDS.toLong())
        assert(1 == testClient.messagesReceived.size)
        val goodbyeClient : GoodbyeClient = gson.fromJson(testClient.messagesReceived.poll(), GoodbyeClient::class.java)
        assert(goodbyeClient.goodbyeMessage == "Servus!")
        assert(testClient.connectionClosed)
        testClient.close()
        server.stop()
    }

    companion object {

        const val DELAY_TIME_MILLISECONDS = 200
    }
}