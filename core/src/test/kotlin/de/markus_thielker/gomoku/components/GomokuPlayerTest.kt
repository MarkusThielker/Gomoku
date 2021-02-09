package de.markus_thielker.gomoku.components

import org.junit.Test

class GomokuPlayerTest {

    @Test
    fun updateStatePlacedTest() {

        val player = GomokuPlayer("Player", GomokuFieldColor.Black)

        player.updateState(arrayOf(1, 1), 2, null)

        assert(player.stones.isNotEmpty() && player.maximum == 2 && player.placed == 1 && player.wins == 0 && player.streak == 0)
    }

    @Test
    fun updateStateWinTest() {

        val player = GomokuPlayer("Player", GomokuFieldColor.Black)

        player.updateState(arrayOf(1, 1), 2, true)

        assert(player.stones.isNotEmpty() && player.maximum == 2 && player.placed == 1 && player.wins == 1 && player.streak == 1)
    }

    @Test
    fun updateStateLoseTest() {

        val player = GomokuPlayer("Player", GomokuFieldColor.Black)

        player.updateState(arrayOf(1, 1), 2, false)

        assert(player.stones.isNotEmpty() && player.maximum == 2 && player.placed == 1 && player.wins == 0 && player.streak == 0)
    }

    @Test
    fun updateStateStreakResetTest() {

        val player = GomokuPlayer("Player", GomokuFieldColor.Black)

        player.updateState(arrayOf(1, 1), 2, true)

        player.clearStats()

        player.updateState(arrayOf(1, 1), 2, false)

        assert(player.stones.isNotEmpty() && player.maximum == 2 && player.placed == 1 && player.wins == 1 && player.streak == 0)
    }

    @Test
    fun clearStatsTest() {

        val player = GomokuPlayer("Player", GomokuFieldColor.Black)

        player.updateState(arrayOf(1, 1), 1, false)
        player.updateState(arrayOf(1, 2), 2, true)

        player.clearStats()

        assert(player.stones.isEmpty() && player.maximum == 0 && player.placed == 0 && player.wins == 1 && player.streak == 1)
    }

}