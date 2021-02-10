package de.markus_thielker.gomoku.components

/**
 * Field and line connection algorithm for fast evaluation of links and lengths
 *
 * @author Markus Thielker
 *
 * */
class GomokuField(val pos : Array<Int>, var color : GomokuFieldColor) {

    // map containing all connections
    val con = HashMap<GomokuFieldDirection, GomokuField?>()

    // map containing the length for each connection
    val conLen = HashMap<GomokuFieldDirection, Int>()

    init {

        // set default values
        conLen[GomokuFieldDirection.Horizontal] = 1
        conLen[GomokuFieldDirection.Vertical] = 1
        conLen[GomokuFieldDirection.DiagonalTLBR] = 1
        conLen[GomokuFieldDirection.DiagonalBLTR] = 1
    }

    /**
     * This function prints the current state of the field with all information
     * It is used for debugging only and does not need to be transferred to main project
     *
     * @author Markus Thielker
     *
     * */
    fun printField() {

        println()

        println("name: $this")
        println("position: ${pos.contentToString()}")
        println("color: $color")
        println("connections:")
        println("  - Horizontal: ${con[GomokuFieldDirection.Horizontal]} | ${conLen[GomokuFieldDirection.Horizontal]}")
        println("  - Vertical: ${con[GomokuFieldDirection.Vertical]} | ${conLen[GomokuFieldDirection.Vertical]}")
        println("  - DiagonalTLBR: ${con[GomokuFieldDirection.DiagonalTLBR]} | ${conLen[GomokuFieldDirection.DiagonalTLBR]}")
        println("  - DiagonalBLTR: ${con[GomokuFieldDirection.DiagonalBLTR]} | ${conLen[GomokuFieldDirection.DiagonalBLTR]}")
        println()
    }
}