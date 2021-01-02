package de.markus_thielker.gomoku.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import de.markus_thielker.gomoku.Application

// window settings
private const val clientAreaWidth = 1280
private const val clientAreaHeight = 720
private const val windowResizable = false

fun main() {

    val config = LwjglApplicationConfiguration()

    // setting window size to WXGA (HD-ready) 16:9 format
    config.width = clientAreaWidth
    config.height = clientAreaHeight

    // disable window resizing
    config.resizable = windowResizable
    LwjglApplication(Application(), config)
}