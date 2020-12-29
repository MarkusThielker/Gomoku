package de.markus_thielker.gomoku.components

/**
 * The read-only GomokuConfiguration object saves all information entered in setup view and gets passed
 * to game view for GomokuGame.kt initialization.
 *
 * @author Markus Thielker
 *
 */
class GomokuConfiguration(
    val playerNameOne : String,
    val playerColorOne : GomokuFieldColor,
    val playerNameTwo : String,
    val playerColorTwo : GomokuFieldColor,
    val opening : GomokuOpening
)