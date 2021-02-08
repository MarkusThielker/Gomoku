package de.markus_thielker.gomoku.components

import de.markus_thielker.gomoku.Application
import de.markus_thielker.gomoku.views.GameView
import org.junit.Test

class GomokuGameTest {

    @Test
    fun stonePlacedTest() {

        val opening = GomokuOpening.Standard
        val p1 = GomokuPlayer("Player 1", GomokuFieldColor.Black)
        val p2 = GomokuPlayer("Player 2", GomokuFieldColor.White)

        val game = GomokuGame(GameView(Application(), opening, p1, p2), opening, p1, p2)

        game.stonePlaced(1, 1)

        assert(game.board[1][1]!!.color == p1.color)
    }

    @Test
    fun checkWinConditionTest() {

        val opening = GomokuOpening.Standard
        val p1 = GomokuPlayer("Player 1", GomokuFieldColor.Black)
        val p2 = GomokuPlayer("Player 2", GomokuFieldColor.White)

        val game = GomokuGame(GameView(Application(), opening, p1, p2), opening, p1, p2)

        game.stonePlaced(2, 1)

        game.stonePlaced(2, 5)

        game.stonePlaced(3, 1)

        game.stonePlaced(3, 3)

        game.stonePlaced(1, 1)

        game.stonePlaced(1, 3)

        game.stonePlaced(5, 1)

        game.stonePlaced(5, 3)

        game.stonePlaced(4, 1)

        game.stonePlaced(4, 5)

        assert(game.gameOver && game.winnerPlayer != null)
    }
}