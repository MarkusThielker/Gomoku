package de.markus_thielker.gomoku.socket

/**
 * This interface handles the connection between GUI and network client.
 *
 * @author Markus Thielker
 *
 * */
interface NetworkController {

    /** This function is called by the SimpleClient when a ping response is received */
    fun onPingResponse(ping : Long)

    /** This function is called by the SimpleClient when the server returns positiv feedback on a history push */
    fun onHistorySaved()

    /** This function is called by the SimpleClient when the server returns negative feedback on a history push */
    fun onHistoryNotSaved()
}