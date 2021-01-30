package de.markus_thielker.gomoku.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import de.markus_thielker.gomoku.Application
import de.markus_thielker.gomoku.socket.SimpleClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URI
import kotlin.system.exitProcess

/**
 * Main navigation instance used for starting games and checking the connection
 *
 * @param application works as navigation host and storage for global variables
 *
 * @author Markus Thielker
 *
 */
class MenuView(private val application : Application) : ApplicationView() {

    private lateinit var stageMenu : Stage

    private lateinit var lblMenuHeading : Label
    private lateinit var btnMenuGameView : TextButton
    private lateinit var btnMenuConnectionTest : TextButton
    private lateinit var btnCloseApplication : TextButton

    /**
     * Automatic lifecycle call when the screen becomes visible.
     *
     * @author Markus Thielker
     *
     * */
    override fun show() {

        // default settings
        stageMenu = Stage()
        Gdx.input.inputProcessor = stageMenu

        // create game view title label
        lblMenuHeading = Label("Gomoku", application.skin)
        lblMenuHeading.apply {
            style = generateLabelStyle(text.toString(), 120, bold = true, border = true)
            setPosition((Gdx.graphics.width / 2).toFloat(), (Gdx.graphics.height / 2 + 170).toFloat(), Align.center)
        }

        // create menu game view button
        btnMenuGameView = TextButton("Spielen!", application.skin)
        btnMenuGameView.apply {
            label.style = generateLabelStyle(text.toString())
            setSize(300f, 30f)
            setPosition((Gdx.graphics.width / 2).toFloat() - (btnMenuGameView.width / 2), (Gdx.graphics.height / 2).toFloat())
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent, x : Float, y : Float) {
                    application.screen = SetupView(application)
                }
            })
        }

        // create connection test button
        btnMenuConnectionTest = TextButton("Server-Verbindung überprüfen", application.skin)
        btnMenuConnectionTest.apply {
            label.style = generateLabelStyle(text.toString())
            setSize(300f, 30f)
            setPosition((Gdx.graphics.width / 2).toFloat() - (btnMenuConnectionTest.width / 2), (Gdx.graphics.height / 2).toFloat() - 40)
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent, x : Float, y : Float) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {

                            val client = SimpleClient(URI(String.format("ws://%s:%d", "localhost", 42000)))

                            client.connect()

                            delay(500)

                            client.closeSession()

                        } catch (exception : Exception) {
                            exception.printStackTrace()
                        }
                    }
                }
            })
        }

        // create menu game view button
        btnCloseApplication = TextButton("Gomoku verlassen", application.skin)
        btnCloseApplication.apply {
            label.style = generateLabelStyle(text.toString())
            setSize(300f, 30f)
            setPosition((Gdx.graphics.width / 2).toFloat() - (btnMenuGameView.width / 2), (Gdx.graphics.height / 2).toFloat() - 80)
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent, x : Float, y : Float) {
                    exitProcess(1)
                }
            })
        }

        // add widgets to menu stage
        stageMenu.addActor(lblMenuHeading)
        stageMenu.addActor(btnMenuGameView)
        stageMenu.addActor(btnMenuConnectionTest)
        stageMenu.addActor(btnCloseApplication)
    }

    /**
     * Automatic lifecycle call for rendering the view. Called multiple times per second.
     *
     * @author Markus Thielker
     *
     * */
    override fun render(delta : Float) {

        // clear view before redrawing
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stageMenu.act()

        // draw background to batch
        stageMenu.batch.begin()
        stageMenu.batch.draw(application.backgroundTexture, 0f, 0f)
        stageMenu.batch.draw(application.backgroundTexture, 0f, Gdx.graphics.height.toFloat())
        stageMenu.batch.end()

        stageMenu.draw()
    }

    /**
     * Automatic lifecycle call when the screen becomes invisible.
     *
     * @author Markus Thielker
     *
     * */
    override fun dispose() {

        // dispose visible views
        stageMenu.dispose()
    }
}