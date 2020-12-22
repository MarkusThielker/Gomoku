package de.markus_thielker.gomoku.components

/**
 * This enum defines the state of a gomoku field
 *
 * Black - The field is already blocked by a black stone
 * White - The field is already blocked by a white stone
 * None  - The field is not blocked yet
 *
 * @author Markus Thielker
 *
 * */
enum class GomokuField {

    Black, White, None
}