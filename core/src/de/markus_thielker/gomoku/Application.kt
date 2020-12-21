package de.markus_thielker.gomoku

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
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

    /**
     * Called on lifecycle startup, updating skin and loading first navigation instance
     *
     * @author Markus Thielker
     *
     * */
    override fun create() {
        skin = Skin(Gdx.files.internal("skins/uiskin.json"))
        setScreen(MenuView(this))
    }
}