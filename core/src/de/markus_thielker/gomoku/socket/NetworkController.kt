package de.markus_thielker.gomoku.socket

interface NetworkController {

    fun pongReceived(ping : Long)
}