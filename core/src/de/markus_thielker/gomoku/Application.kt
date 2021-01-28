package de.markus_thielker.gomoku

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import de.markus_thielker.gomoku.views.MenuView

/**
 * Application layer, working as navigation service for the application.
 *
 * @author Markus Thielker
 *
 * */
class Application : Game() {

    lateinit var skin : Skin

    lateinit var backgroundTexture : TextureRegion

    /**
     * Called on lifecycle startup, updating skin and loading first navigation instance
     *
     * @author Markus Thielker
     *
     * */
    override fun create() {

        // load application skin
        skin = Skin(Gdx.files.internal("skins/uiskin.json"))

        // load background texture
        backgroundTexture = TextureRegion(Texture("img/wood_background.jpg"), 0, 0, 1280, 720)

        // initial navigation
        setScreen(MenuView(this))
    }
}