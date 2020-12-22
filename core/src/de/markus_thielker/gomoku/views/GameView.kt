package de.markus_thielker.gomoku.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import de.markus_thielker.gomoku.Application
import de.markus_thielker.gomoku.components.GomokuConfiguration
import de.markus_thielker.gomoku.components.GomokuGame
import java.util.*

/**
 * This screen shows the current gameplay state to give players possibility to interact with the game logic
 *
 * @param application works as navigation host and storage for global variables
 *
 * @author Markus Thielker
 *
 */
class GameView(private val application : Application, config : GomokuConfiguration) : ScreenAdapter() {

    private val gameplay = GomokuGame(config)
    private var gamePaused = false

    // scanner for input simulation
    private val scanner = Scanner(System.`in`)

    private lateinit var stageGame : Stage
    private lateinit var btnGamePause : TextButton

    private lateinit var dialogPause : Dialog
    private lateinit var lblPauseHeading : Label
    private lateinit var btnPauseContinue : TextButton
    private lateinit var btnPauseBack : TextButton

    override fun show() {

        // default settings
        stageGame = Stage()
        Gdx.input.inputProcessor = stageGame

        // create game view pause button
        btnGamePause = TextButton("II", application.skin) // TODO: change string to icon
        btnGamePause.setSize(30f, 30f)
        btnGamePause.setPosition((Gdx.graphics.width - 30).toFloat(), (Gdx.graphics.height - 30).toFloat())
        btnGamePause.addListener(object : ClickListener() {
            override fun clicked(event : InputEvent, x : Float, y : Float) {
                switchPause()
            }
        })

        // add widgets to game stage
        stageGame.addActor(btnGamePause)

        // TODO: display game board on screen

        // setup pause stage
        dialogPause = setupPauseDialog()
        stageGame.addActor(dialogPause)
    }

    override fun render(delta : Float) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stageGame.act()
        stageGame.draw()

        // SIMULATE INPUT VIA CONSOLE
        if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE) && !gamePaused) {

            print("x: ")
            val inY = Integer.parseInt(scanner.nextLine()) - 1

            print("y: ")
            val inX = Integer.parseInt(scanner.nextLine()) - 1

            gameplay.stonePlaced(inX, inY) // flipped [x,y] due to debugging print direction
        }
    }

    override fun dispose() {
        stageGame.dispose()
    }

    private fun setupPauseDialog() : Dialog {

        // create pause dialog
        val dialog = Dialog("Paused", application.skin)
        dialog.setSize(400f, 200f)
        dialog.setPosition(Gdx.graphics.width.toFloat() / 2 - 200, Gdx.graphics.height.toFloat() / 2 - 100)
        dialog.isVisible = false
        dialog.isMovable = false

        // create pause dialog title label
        lblPauseHeading = Label("Pause", application.skin)

        // create pause dialog continue button
        btnPauseContinue = TextButton("Continue", application.skin)
        btnPauseContinue.setSize(150f, 30f)
        btnPauseContinue.addListener(object : ClickListener() {
            override fun clicked(event : InputEvent, x : Float, y : Float) {
                switchPause()
            }
        })

        // create pause dialog back button
        btnPauseBack = TextButton("Back to Menu", application.skin)
        btnPauseBack.setSize(150f, 30f)
        btnPauseBack.addListener(object : ClickListener() {
            override fun clicked(event : InputEvent, x : Float, y : Float) {
                application.screen = MenuView(application)
            }
        })

        // add widgets to dialog
        dialog.contentTable.add(lblPauseHeading)
        dialog.contentTable.add(btnPauseContinue)
        dialog.contentTable.add(btnPauseBack)

        return dialog
    }

    private fun switchPause() {

        gamePaused = !gamePaused
        dialogPause.isVisible = gamePaused
    }
}