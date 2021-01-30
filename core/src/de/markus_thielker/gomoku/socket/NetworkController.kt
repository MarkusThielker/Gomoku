package de.markus_thielker.gomoku.socket

interface NetworkController {

    fun onPingResponse(ping : Long)

    fun onHistorySaved()

    fun onHistoryNotSaved()
}