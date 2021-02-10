package de.markus_thielker.gomoku.components

import de.markus_thielker.gomoku.Application
import de.markus_thielker.gomoku.socket.SimpleClient
import de.markus_thielker.gomoku.socket.TestServer
import de.markus_thielker.gomoku.views.GameView
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetSocketAddress
import java.net.URI

class SimpleClientTest {

    @Test
    fun pushHistorySuccessTest() {

        val address = "localhost"
        val port = 42001

        val server = TestServer(InetSocketAddress(address, port))
        server.start()

        Thread.sleep(500)

        val opening = GomokuOpening.Standard
        val p1 = GomokuPlayer("Player 1", GomokuFieldColor.Black)
        val p2 = GomokuPlayer("Player 2", GomokuFieldColor.White)

        val gameView = GameView(Application(), opening, p1, p2)

        val game = GomokuGame(gameView, opening, p1, p2)

        // create client socket focusing on local server
        val client = SimpleClient(URI(String.format("ws://%s:%d", address, port)), game)

        // connect to passed connection
        client.connect()

        Thread.sleep(500)

        client.pushMatchResult(p1.name, p2.name, playerOneWinner = true, playerTwoWinner = false)

        // timeout to ensure connection
        Thread.sleep(500)

        // close connection by sending goodbye to server
        client.closeSession()

        Thread.sleep(500)

        assertTrue(server.connectionOpened && server.connectionClosed && !server.exceptionOccurred && server.historySaved)
    }

    @Test
    fun pushHistoryErrorTest() {

        val address = "localhost"
        val port = 42002

        val server = TestServer(InetSocketAddress(address, port))
        server.start()

        Thread.sleep(500)

        val opening = GomokuOpening.Standard
        val p1 = GomokuPlayer("Player 1", GomokuFieldColor.Black)
        val p2 = GomokuPlayer("Player 2", GomokuFieldColor.White)

        val gameView = GameView(Application(), opening, p1, p2)

        val game = GomokuGame(gameView, opening, p1, p2)

        // create client socket focusing on local server
        val client = SimpleClient(URI(String.format("ws://%s:%d", address, port)), game)

        // connect to passed connection
        client.connect()

        Thread.sleep(500)

        client.pushMatchResult(p1.name, p2.name, playerOneWinner = true, playerTwoWinner = true)

        // timeout to ensure connection
        Thread.sleep(500)

        // close connection by sending goodbye to server
        client.closeSession()

        Thread.sleep(500)

        assertTrue(server.connectionOpened && server.connectionClosed && !server.exceptionOccurred && server.historyNotSaved)
    }

    @Test
    fun sendPingTest() {

        val address = "localhost"
        val port = 42003

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

        assert(server.connectionOpened && server.connectionClosed && !server.exceptionOccurred && server.pingRequested)
    }

    @Test
    fun autoDisconnectionTest() {

        val address = "localhost"
        val port = 42004

        val server = TestServer(InetSocketAddress(address, port))
        server.start()

        server.forceTimeout = true

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

        // close connection by sending goodbye to server
        client.closeSession()

        assert(server.connectionOpened && !server.exceptionOccurred && server.sessionClosed && client.connectionTimeout)
    }
}