package de.markus_thielker.gomoku.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import de.markus_thielker.gomoku.Application

/**
 * Main navigation instance used for starting games and checking the connection
 *
 * @param application works as navigation host and storage for global variables
 *
 * @author Markus Thielker
 *
 */
class MenuView(private val application : Application) : ScreenAdapter() {

    private lateinit var stageMenu : Stage

    private lateinit var lblMenuHeading : Label
    private lateinit var btnMenuGameView : TextButton
    private lateinit var btnMenuConnectionTest : TextButton

    override fun show() {

        // default settings
        stageMenu = Stage()
        Gdx.input.inputProcessor = stageMenu


        // create game view title label
        lblMenuHeading = Label("MenuView", application.skin)
        lblMenuHeading.setPosition((Gdx.graphics.width / 2).toFloat() - (lblMenuHeading.width / 2), (Gdx.graphics.height / 2).toFloat() + 80)


        // create menu game view button
        btnMenuGameView = TextButton("Play!", application.skin)
        btnMenuGameView.setSize(150f, 30f)
        btnMenuGameView.setPosition((Gdx.graphics.width / 2).toFloat() - (btnMenuGameView.width / 2), (Gdx.graphics.height / 2).toFloat() + 40)
        btnMenuGameView.addListener(object : ClickListener() {
            override fun clicked(event : InputEvent, x : Float, y : Float) {
                application.screen = SetupView(application)
            }
        })


        // create connection test button
        btnMenuConnectionTest = TextButton("Check connection", application.skin)
        btnMenuConnectionTest.setSize(150f, 30f)
        btnMenuConnectionTest.setPosition((Gdx.graphics.width / 2).toFloat() - (btnMenuConnectionTest.width / 2), (Gdx.graphics.height / 2).toFloat())
        btnMenuConnectionTest.addListener(object : ClickListener() {
            override fun clicked(event : InputEvent, x : Float, y : Float) {
                // TODO: initiate connection check
            }
        })


        // add widgets to menu stage
        stageMenu.addActor(lblMenuHeading)
        stageMenu.addActor(btnMenuGameView)
        stageMenu.addActor(btnMenuConnectionTest)
    }

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

    override fun dispose() {

        // dispose visible views
        stageMenu.dispose()
    }
}