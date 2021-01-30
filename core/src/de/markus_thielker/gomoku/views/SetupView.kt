package de.markus_thielker.gomoku.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import de.markus_thielker.gomoku.Application
import de.markus_thielker.gomoku.components.GomokuFieldColor
import de.markus_thielker.gomoku.components.GomokuOpening
import de.markus_thielker.gomoku.components.GomokuPlayer

/**
 * This screen is shown before starting a game of gomoku to setup the game configuration
 *
 * @param application works as navigation host and storage for global variables
 *
 * @author Markus Thielker
 *
 */
class SetupView(private val application : Application) : ApplicationView() {

    private lateinit var stageSetup : Stage

    private lateinit var txtNameOne : TextField
    private lateinit var txtNameTwo : TextField
    private lateinit var selectOpening : SelectBox<GomokuOpening>
    private lateinit var btnStartGame : TextButton

    private lateinit var btnBack : TextButton

    /**
     * Automatic lifecycle call when screen becomes visible.
     *
     * @author Markus Thielker
     *
     * */
    override fun show() {

        // get input for name of p1
        txtNameOne = TextField("", application.skin)
        txtNameOne.apply {
            text = "Spieler 1"
            maxLength = 15
            setSize(150f, 30f)
            setPosition((Gdx.graphics.width).toFloat() / 2 - 5, (Gdx.graphics.height).toFloat() / 2 + 15, Align.bottomRight)
        }

        // get input for name of p2
        txtNameTwo = TextField("", application.skin)
        txtNameTwo.apply {
            text = "Spieler 2"
            maxLength = 15
            setSize(150f, 30f)
            setPosition((Gdx.graphics.width).toFloat() / 2 + 5, (Gdx.graphics.height).toFloat() / 2 + 15, Align.bottomLeft)
        }

        // get input for game opening rule
        selectOpening = SelectBox<GomokuOpening>(application.skin)
        selectOpening.apply {
            items = Array(arrayOf(GomokuOpening.Standard, GomokuOpening.Swap2))
            setSize(310f, 30f)
            setPosition((Gdx.graphics.width).toFloat() / 2, (Gdx.graphics.height).toFloat() / 2 - 10, Align.center)
        }

        // get input to start game
        btnStartGame = TextButton("Spiel starten", application.skin)
        btnStartGame.apply {
            label.style = generateLabelStyle(text.toString())
            setSize(310f, 30f)
            setPosition((Gdx.graphics.width).toFloat() / 2, (Gdx.graphics.height).toFloat() / 2 - 50, Align.center)
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent?, x : Float, y : Float) {
                    application.screen =
                        GameView(
                            application,
                            selectOpening.selected,
                            GomokuPlayer(txtNameOne.text, GomokuFieldColor.Black),
                            GomokuPlayer(txtNameTwo.text, GomokuFieldColor.White),
                        )
                }
            })
        }

        // get input to start game
        btnBack = TextButton("Zurück zum Menü", application.skin)
        btnBack.apply {
            label.style = generateLabelStyle(text.toString())
            setSize(200f, 30f)
            setPosition(30f, 30f, Align.bottomLeft)
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent?, x : Float, y : Float) {
                    application.screen = MenuView(application)
                }
            })
        }

        // add actors to stage
        stageSetup = Stage()
        Gdx.input.inputProcessor = stageSetup

        stageSetup.addActor(txtNameOne)
        stageSetup.addActor(txtNameTwo)
        stageSetup.addActor(selectOpening)
        stageSetup.addActor(btnStartGame)

        stageSetup.addActor(btnBack)
    }

    /**
     * Automatic lifecycle call for rendering the view. Called multiple times per second.
     *
     * @author Markus Thielker
     *
     * */
    override fun render(delta : Float) {

        stageSetup.act()

        // draw background to batch
        stageSetup.batch.begin()
        stageSetup.batch.draw(application.backgroundTexture, 0f, 0f)
        stageSetup.batch.draw(application.backgroundTexture, 0f, Gdx.graphics.height.toFloat())
        stageSetup.batch.end()

        stageSetup.draw()
    }

    /**
     * Automatic lifecycle call when the screen becomes invisible.
     *
     * @author Markus Thielker
     *
     * */
    override fun dispose() {
        stageSetup.dispose()
    }
}