package de.markus_thielker.gomoku.socket

interface NetworkController {

    fun onPingResponse(ping : Long)
}