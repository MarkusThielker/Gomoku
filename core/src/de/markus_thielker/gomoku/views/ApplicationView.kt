package de.markus_thielker.gomoku.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class ApplicationView : ScreenAdapter() {

    /**
     * Display a message in the bottom of the screen.
     *
     * @param stage stage, the message gets displayed on
     * @param title title of the dialog
     * @param message message of the dialog
     * @param skin skin of the dialog
     *
     * @author Markus Thielker
     *
     * */
    fun showMessage(stage : Stage, title : String, message : String, skin : Skin) {

        // launch coroutine to not block main thread
        GlobalScope.launch {

            // create dialog
            val dialog = Dialog(title, skin)

            // add message to dialog
            dialog.contentTable.addActor(Label(message, skin))

            // show dialog
            dialog.show(stage)

            dialog.width = 500f
            dialog.height = 60f

            // reposition dialog
            dialog.setPosition((Gdx.graphics.width / 2).toFloat(), 40f, Align.center)

            delay(2500)

            dialog.hide()
        }
    }
}