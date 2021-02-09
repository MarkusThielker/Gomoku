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
    fun checkWinConditionHorizontalTest() {

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

        assert(game.gameOver && game.winnerPlayer == game.playerOne)
    }

    @Test
    fun checkWinConditionVerticalTest() {

        val opening = GomokuOpening.Standard
        val p1 = GomokuPlayer("Player 1", GomokuFieldColor.Black)
        val p2 = GomokuPlayer("Player 2", GomokuFieldColor.White)

        val game = GomokuGame(GameView(Application(), opening, p1, p2), opening, p1, p2)

        game.stonePlaced(1, 6)

        game.stonePlaced(2, 6)

        game.stonePlaced(1, 5)

        game.stonePlaced(2, 5)

        game.stonePlaced(1, 4)

        game.stonePlaced(2, 4)

        game.stonePlaced(1, 3)

        game.stonePlaced(2, 3)

        game.stonePlaced(1, 2)

        assert(game.gameOver && game.winnerPlayer == game.playerOne)
    }

    @Test
    fun checkWinConditionDiagonalTLBRTest() {

        val opening = GomokuOpening.Standard
        val p1 = GomokuPlayer("Player 1", GomokuFieldColor.Black)
        val p2 = GomokuPlayer("Player 2", GomokuFieldColor.White)

        val game = GomokuGame(GameView(Application(), opening, p1, p2), opening, p1, p2)

        game.stonePlaced(1, 1)

        game.stonePlaced(1, 2)

        game.stonePlaced(2, 2)

        game.stonePlaced(2, 3)

        game.stonePlaced(3, 3)

        game.stonePlaced(3, 4)

        game.stonePlaced(4, 4)

        game.stonePlaced(4, 5)

        game.stonePlaced(5, 5)

        assert(game.gameOver && game.winnerPlayer == game.playerOne)
    }

    @Test
    fun checkWinConditionDiagonalBLTR() {

        val opening = GomokuOpening.Standard
        val p1 = GomokuPlayer("Player 1", GomokuFieldColor.Black)
        val p2 = GomokuPlayer("Player 2", GomokuFieldColor.White)

        val game = GomokuGame(GameView(Application(), opening, p1, p2), opening, p1, p2)

        game.stonePlaced(1, 5)

        game.stonePlaced(1, 6)

        game.stonePlaced(2, 4)

        game.stonePlaced(2, 5)

        game.stonePlaced(3, 3)

        game.stonePlaced(3, 4)

        game.stonePlaced(4, 2)

        game.stonePlaced(4, 3)

        game.stonePlaced(5, 1)

        assert(game.gameOver && game.winnerPlayer == game.playerOne)
    }
}