package de.markus_thielker.gomoku.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import de.markus_thielker.gomoku.Application

/**
 * This screen is shown before starting a game of gomoku to setup the game configuration
 *
 * @param application works as navigation host and storage for global variables
 *
 * @author Markus Thielker
 *
 */
class SetupView(private val application : Application) : ScreenAdapter() {

    private lateinit var stageSetup : Stage
    private lateinit var button : TextButton

    override fun show() {

        // add widgets to stageSetup
        stageSetup = Stage()
        Gdx.input.inputProcessor = stageSetup

        // get input to start game
        button = TextButton("Start the game", application.skin)
        button.setSize(300f, 30f)
        button.setPosition((Gdx.graphics.width).toFloat() / 2, (Gdx.graphics.height).toFloat() / 2 - 75, Align.center)
        button.addListener(object : ClickListener() {
            override fun clicked(event : InputEvent?, x : Float, y : Float) {
                application.screen = GameView(application /* TODO: pass configuration, with values from input */)
            }
        })

        stageSetup.addActor(button)
    }

    override fun render(delta : Float) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stageSetup.act()
        stageSetup.draw()
    }

    override fun dispose() {
        stageSetup.dispose()
    }
}