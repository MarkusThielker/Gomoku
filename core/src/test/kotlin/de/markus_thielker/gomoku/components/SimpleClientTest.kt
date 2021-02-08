package de.markus_thielker.gomoku.components

import de.markus_thielker.gomoku.Application
import de.markus_thielker.gomoku.socket.SimpleClient
import de.markus_thielker.gomoku.views.GameView
import org.junit.Test
import java.net.URI

class SimpleClientTest {

    @Test
    fun pushHistoryTest() {

        val opening = GomokuOpening.Standard
        val p1 = GomokuPlayer("Player 1", GomokuFieldColor.Black)
        val p2 = GomokuPlayer("Player 2", GomokuFieldColor.White)

        val game = GomokuGame(GameView(Application(), opening, p1, p2), opening, p1, p2)

        // create client socket focusing on local server
        val client = SimpleClient(URI(String.format("ws://%s:%d", "localhost", 42000)), game)

        // connect to passed connection
        client.connect()

        Thread.sleep(500)

        // get winner and push to server
        client.pushMatchResult("player1", "player2", true, false)

        // timeout to ensure connection
        Thread.sleep(500)

        // close connection by sending goodbye to server
        client.closeSession()
    }

    @Test
    fun sendPingTest() {

        val address = "localhost"
        val port = 42002

        val server = TestServer(InetSocketAddress(address, port))
        server.start()

        Thread.sleep(500)

        val opening = GomokuOpening.Standard
        val p1 = GomokuPlayer("Player 1", GomokuFieldColor.Black)
        val p2 = GomokuPlayer("Player 2", GomokuFieldColor.White)

        val game = GomokuGame(GameView(Application(), opening, p1, p2), opening, p1, p2)

        // create client socket focusing on local server
        val client = SimpleClient(URI(String.format("ws://%s:%d", address, port)), game)

        // connect to passed connection
        client.connect()

        Thread.sleep(500)

        client.requestPing()

        // timeout to ensure connection
        Thread.sleep(500)

        // close connection by sending goodbye to server
        client.closeSession()

        Thread.sleep(1000)

        assert(server.connectionOpened && server.connectionClosed && !server.exceptionOccurred && server.messagesReceived.isNotEmpty())
    }
}