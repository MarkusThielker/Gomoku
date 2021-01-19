package io.swapastack.gomoku.shared

/**
 * This class represents the HelloServer message specified in the network standard.
 * This is the first message that the clients sends to the game history server after the
 * WebSocket connection is established.
 * This is the only client message that does not need the userId UUID.
 * This is because the userId UUID is contained in the servers answer too this message.
 *
 * @author Dennis Jehle
 */
class HelloServer {

    var messageType = MessageType.HelloServer
}