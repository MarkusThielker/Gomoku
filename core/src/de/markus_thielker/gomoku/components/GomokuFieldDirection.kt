package de.markus_thielker.gomoku.components

/**
 * Connected lines on the game board are limited to these directions.
 * The enum is used for tracing the lines and distinguish the node links.
 *
 * @author Markus Thielker
 *
 */
enum class GomokuFieldDirection {

    Horizontal, Vertical, DiagonalTLBR, DiagonalBLTR
}