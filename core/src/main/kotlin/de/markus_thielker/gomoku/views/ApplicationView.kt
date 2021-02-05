package de.markus_thielker.gomoku.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class ApplicationView : ScreenAdapter() {

    fun generateLabelStyle(value : String, size : Int = 15, color : Color = Color.WHITE, bold : Boolean = false, border : Boolean = false) : LabelStyle {

        val bitmapFontGenerator = if (bold) FreeTypeFontGenerator(Gdx.files.internal("fonts/JetBrainsMono-ExtraBold.ttf"))
        else FreeTypeFontGenerator(Gdx.files.internal("fonts/JetBrainsMono-Bold.ttf"))

        // specify parameters for BitmapFont generation
        val bitmapFontParameter = FreeTypeFontParameter()

        // set font size
        bitmapFontParameter.size = size

        // specify available letters
        bitmapFontParameter.characters = value

        // set font color in RGBA format (red, green, blue, alpha)
        bitmapFontParameter.color = color

        if (border) {
            bitmapFontParameter.borderWidth = 1f
            bitmapFontParameter.borderColor = Color.BLACK // alternative enum color specification
        }

        // generate BitmapFont with FreeTypeFontGenerator and FreeTypeFontParameter specification
        val font : BitmapFont = bitmapFontGenerator.generateFont(bitmapFontParameter)

        // create a LabelStyle object to specify Label font
        val labelStyle = LabelStyle()
        labelStyle.font = font

        return labelStyle
    }

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

            dialog.isMovable = false
            dialog.isModal = false

            dialog.width = 500f
            dialog.height = 60f

            // reposition dialog
            dialog.setPosition((Gdx.graphics.width / 2).toFloat(), 40f, Align.center)

            delay(2500)

            dialog.hide()
        }
    }
}