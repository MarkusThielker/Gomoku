package de.markus_thielker.gomoku.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import de.markus_thielker.gomoku.Application
import de.markus_thielker.gomoku.components.GomokuFieldColor
import de.markus_thielker.gomoku.components.GomokuGame
import de.markus_thielker.gomoku.components.GomokuOpening
import de.markus_thielker.gomoku.components.GomokuPlayer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * This screen shows the current gameplay state to give players possibility to interact with the game logic
 *
 * @param application works as navigation host and storage for global variables
 *
 * @author Markus Thielker
 *
 */
class GameView(val application : Application, var opening : GomokuOpening, playerOne : GomokuPlayer, playerTwo : GomokuPlayer) : ApplicationView() {

    // game interaction essentials
    private val gameplay = GomokuGame(this, opening, playerOne, playerTwo)
    private var gamePaused = false

    private lateinit var activeStage : Stage

    // widgets for game visualization
    private lateinit var stageGame : Stage
    private lateinit var lblPlayerNameOne : Label
    private lateinit var lblPlayerNameTwo : Label
    private lateinit var btnGamePause : ImageButton
    private lateinit var camera : OrthographicCamera
    private lateinit var viewport : Viewport
    private lateinit var batch : SpriteBatch
    private lateinit var shapeRenderer : ShapeRenderer

    // widgets for pause dialog
    private lateinit var dialogPause : Dialog
    private lateinit var lblPauseHeading : Label
    private lateinit var btnPauseContinue : TextButton
    private lateinit var btnPauseRestart : TextButton
    private lateinit var btnPauseBack : TextButton

    // widgets for game over screen
    private lateinit var stageOver : Stage
    private lateinit var lblGameOver : Label
    private lateinit var btnReplay : TextButton
    private lateinit var btnBackToMenu : TextButton

    // grid dimensions
    val gridSize = 15
    private val padding = 100f
    private val lineWidth = 5f

    // click cooldown
    private var cooldown = false
    private val cooldownLength : Long = 250

    /**
     * Automatic lifecycle call when screen becomes visible.
     *
     * @author Markus Thielker
     *
     * */
    override fun show() {

        // initialize using current screen size
        camera = OrthographicCamera(Gdx.graphics.height.toFloat(), Gdx.graphics.width.toFloat())

        // initialize ScreenViewport
        viewport = ScreenViewport(camera)

        // initialize SpriteBatch
        batch = SpriteBatch()

        // initialize ShapeRenderer
        shapeRenderer = ShapeRenderer()

        // init stage and set inputProcessor
        stageGame = Stage(viewport, batch)
        Gdx.input.inputProcessor = stageGame

        activeStage = stageGame

        // create player one overview
        lblPlayerNameOne = Label("${gameplay.playerOne.name} | Siege: ${gameplay.playerOne.wins} | Sieges-Serie: ${gameplay.playerOne.streak}", application.skin)
        lblPlayerNameOne.apply {
            style = generateLabelStyle(text.toString(), bold = true)
            setPosition((Gdx.graphics.width / 2 - 15 - lblPlayerNameOne.width / 2), (Gdx.graphics.height - 50).toFloat(), Align.right)
        }

        // create player two overview
        lblPlayerNameTwo = Label("${gameplay.playerTwo.name} | Siege: ${gameplay.playerTwo.wins} | Sieges-Serie: ${gameplay.playerTwo.streak}", application.skin)
        lblPlayerNameTwo.apply {
            style = generateLabelStyle(text.toString(), bold = true)
            setPosition((Gdx.graphics.width / 2 + 15 + lblPlayerNameTwo.width / 2), (Gdx.graphics.height - 50).toFloat(), Align.left)
        }

        // create game view pause button
        val drawable : Drawable = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("img/button_pause.png"))))
        btnGamePause = ImageButton(drawable)
        btnGamePause.apply {
            setSize(35f, 35f)
            setPosition((Gdx.graphics.width - 45).toFloat(), (Gdx.graphics.height - 45).toFloat())
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent, x : Float, y : Float) {
                    switchPause()
                }
            })
        }

        // add widgets to game stage
        stageGame.addActor(lblPlayerNameOne)
        stageGame.addActor(lblPlayerNameTwo)
        stageGame.addActor(btnGamePause)

        // setup pause stage
        dialogPause = setupPauseDialog()
        stageGame.addActor(dialogPause)

        stageOver = setupStageOver()
    }

    /**
     * Automatic lifecycle call for rendering the view. Called multiple times per second.
     *
     * @author Markus Thielker
     *
     * */
    override fun render(delta : Float) {

        activeStage.act()

        // draw background to batch
        activeStage.batch.begin()
        activeStage.batch.draw(application.backgroundTexture, 0f, 0f)
        activeStage.batch.draw(application.backgroundTexture, 0f, Gdx.graphics.height.toFloat())
        activeStage.batch.end()

        if (gameplay.gameOver) {

            activeStage = stageOver
            Gdx.input.inputProcessor = stageOver

            // check if win or tie
            if (gameplay.winnerPlayer != null)
                lblGameOver.apply {
                    setText("${gameplay.winnerPlayer!!.name} hat das Spiel gewonnen!")
                    style = generateLabelStyle(text.toString(), 35, bold = true)
                }


            gamePaused = true

        } else {

            // update camera and batch
            camera.update()
            batch.projectionMatrix = camera.combined

            // get screen data for rendering
            val screenWidth = Gdx.graphics.width.toFloat()
            val screenHeight = Gdx.graphics.height.toFloat()
            val columnHeight = screenHeight - 2f * padding
            val offset = columnHeight / (gridSize - 1f)
            val cornerTL = screenWidth / 2f - columnHeight / 2f

            // get mouse hover
            val mouseX = Gdx.input.x
            val mouseY = Gdx.input.y

            var minDist = sqrt((mouseX - cornerTL) * (mouseX - cornerTL) + (mouseY - padding) * (mouseY - padding))
            var minX = 0
            var minY = 0

            for (i in 0 until gridSize) {

                for (k in 0 until gridSize) {

                    val xDist = mouseX - (cornerTL + i * offset)
                    val yDist = mouseY - (padding + k * offset)
                    val dist = sqrt(xDist * xDist + yDist * yDist)

                    if (dist < minDist) {
                        minDist = dist
                        minX = i
                        minY = k
                    }
                }
            }

            // rendering grid
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            for (i in 0 until gridSize) {
                shapeRenderer.rectLine(
                    cornerTL + i * offset,
                    padding + columnHeight,
                    cornerTL + i * offset,
                    padding,
                    lineWidth,
                    Color.GRAY,
                    Color.GRAY
                )
                shapeRenderer.rectLine(
                    cornerTL,
                    padding + columnHeight - i * offset,
                    cornerTL + columnHeight,
                    padding + columnHeight - i * offset,
                    lineWidth,
                    Color.GRAY,
                    Color.GRAY
                )
            }
            shapeRenderer.end()

            if (!gamePaused) {

                // rendering preview
                if (minDist < 100) {
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
                    shapeRenderer.color = Color.GREEN
                    shapeRenderer.circle((cornerTL + (minX * 37)), Gdx.graphics.height - (padding + (minY * 37)), 7.5f)
                    shapeRenderer.end()
                }

                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && minDist < 100 && !cooldown) {
                    gameplay.stonePlaced(minX, minY)

                    GlobalScope.launch {
                        cooldown = true
                        delay(cooldownLength)
                        cooldown = false
                    }
                }
            }

            // rendering black stones
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color.BLACK
            gameplay.board.forEach { column ->
                column.forEach { field ->
                    if (field != null && field.color == GomokuFieldColor.Black) {
                        shapeRenderer.circle(
                            (cornerTL + (field.pos[0] * 37)), Gdx.graphics.height - (padding + (field.pos[1] * 37)), 18f
                        )
                    }
                }
            }
            shapeRenderer.end()

            // rendering white stones
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color.WHITE
            gameplay.board.forEach { column ->
                column.forEach { field ->
                    if (field != null && field.color == GomokuFieldColor.White) {
                        shapeRenderer.circle(
                            (cornerTL + (field.pos[0] * 37)), Gdx.graphics.height - (padding + (field.pos[1] * 37)), 18f
                        )
                    }
                }
            }
            shapeRenderer.end()

            // rendering turn indicator
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color.RED

            if (gameplay.currentPlayer == gameplay.playerOne) {
                shapeRenderer.rectLine(
                    lblPlayerNameOne.getX(Align.bottomLeft),
                    lblPlayerNameOne.getY(Align.bottomLeft),
                    lblPlayerNameOne.getX(Align.bottomRight),
                    lblPlayerNameOne.getY(Align.bottomRight),
                    4f
                )
            } else {
                shapeRenderer.rectLine(
                    lblPlayerNameTwo.getX(Align.bottomLeft),
                    lblPlayerNameTwo.getY(Align.bottomLeft),
                    lblPlayerNameTwo.getX(Align.bottomRight),
                    lblPlayerNameTwo.getY(Align.bottomRight),
                    4f
                )
            }
            shapeRenderer.end()
        }

        activeStage.draw()
    }

    /**
     * Automatic lifecycle call when the screen becomes invisible.
     *
     * @author Markus Thielker
     *
     * */
    override fun dispose() {
        stageGame.dispose()
    }

    /**
     * Creates the pause dialog for the pauseDialog.
     *
     * @return returns the created stage
     *
     * @author Markus Thielker
     *
     * */
    private fun setupPauseDialog() : Dialog {

        // create pause dialog
        val dialog = Dialog("Spiel pausiert", application.skin)
        dialog.setSize(400f, 200f)
        dialog.setPosition(Gdx.graphics.width.toFloat() / 2 - 200, Gdx.graphics.height.toFloat() / 2 - 100)
        dialog.isVisible = false
        dialog.isMovable = false

        // create pause dialog title label
        lblPauseHeading = Label("Pause", application.skin)
        lblPauseHeading.apply {
            style = generateLabelStyle(text.toString(), bold = true)
            setPosition(dialog.width / 2, dialog.height / 2 + 60, Align.center)
        }

        // create pause dialog continue button
        btnPauseContinue = TextButton("Fortfahren", application.skin)
        btnPauseContinue.apply {
            label.style = generateLabelStyle(text.toString())
            setSize(200f, 30f)
            setPosition(dialog.width / 2, dialog.height / 2 + 20, Align.center)
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent, x : Float, y : Float) {
                    switchPause()
                }
            })
        }

        // create pause dialog restart button
        btnPauseRestart = TextButton("Runde neustarten", application.skin)
        btnPauseRestart.apply {
            label.style = generateLabelStyle(text.toString())
            setSize(200f, 30f)
            setPosition(dialog.width / 2, dialog.height / 2 - 20, Align.center)
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent, x : Float, y : Float) {

                    // create dialog with result callback
                    val confirmationDialog = object : Dialog("Runde neu starten?", application.skin) {

                        // execute on result selected
                        override fun result(result : Any) {

                            // differentiate selections
                            if (result as Boolean) {
                                gameplay.playerOne.clearStats()
                                gameplay.playerTwo.clearStats()

                                application.screen = GameView(application, opening, gameplay.playerOne, gameplay.playerTwo)
                            } else {
                                this.hide()
                            }
                        }
                    }

                    // add buttons to dialog
                    confirmationDialog.button("Neustarten", true)
                    confirmationDialog.button("Abbrechen", false)

                    // show dialog on UI
                    showDialog(confirmationDialog)
                }
            })
        }

        // create pause dialog back button
        btnPauseBack = TextButton("Zurück zum Menü", application.skin)
        btnPauseBack.apply {
            label.style = generateLabelStyle(text.toString())
            setSize(200f, 30f)
            setPosition(dialog.width / 2, dialog.height / 2 - 60, Align.center)
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent, x : Float, y : Float) {

                    // create dialog with result callback
                    val confirmationDialog = object : Dialog("Zurück zum Menü?", application.skin) {

                        // execute on result selected
                        override fun result(result : Any) {

                            // differentiate selections
                            if (result as Boolean) {
                                application.screen = MenuView(application)
                            } else {
                                this.hide()
                            }
                        }
                    }

                    // add buttons to dialog
                    confirmationDialog.button("Zum Menü", true)
                    confirmationDialog.button("Abbrechen", false)

                    // show dialog on UI
                    showDialog(confirmationDialog)
                }
            })
        }

        // add widgets to dialog
        dialog.contentTable.addActor(lblPauseHeading)
        dialog.contentTable.addActor(btnPauseContinue)
        dialog.contentTable.addActor(btnPauseRestart)
        dialog.contentTable.addActor(btnPauseBack)

        return dialog
    }

    /**
     * Create the stage for the gameOverView.
     *
     * @return returns the created stage
     *
     * @author Markus Thielker
     *
     * */
    private fun setupStageOver() : Stage {

        val stage = Stage()

        // create winner label
        lblGameOver = Label("Unentschieden! Klicke auf \"Revanche\" um noch einmal zu spielen.", application.skin)
        lblGameOver.apply {
            style = generateLabelStyle(text.toString(), bold = true)
            setPosition((Gdx.graphics.width).toFloat() / 2, (Gdx.graphics.height).toFloat() / 2 + 30, Align.center)
            setAlignment(Align.center)
        }

        // create button to rematch with same player objects
        btnReplay = TextButton("Revanche", application.skin)
        btnReplay.apply {
            label.style = generateLabelStyle(text.toString())
            setSize(200f, 30f)
            setPosition((Gdx.graphics.width).toFloat() / 2 + 5, (Gdx.graphics.height).toFloat() / 2, Align.topLeft)
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent, x : Float, y : Float) {

                    gameplay.playerOne.color = GomokuFieldColor.Black
                    gameplay.playerTwo.color = GomokuFieldColor.White

                    gameplay.playerOne.clearStats()
                    gameplay.playerTwo.clearStats()

                    application.screen = GameView(application, opening, gameplay.playerOne, gameplay.playerTwo)
                }
            })
        }

        // create button to return to menu
        btnBackToMenu = TextButton("Zurück zum Menü", application.skin)
        btnBackToMenu.apply {
            label.style = generateLabelStyle(text.toString())
            setSize(200f, 30f)
            setPosition((Gdx.graphics.width).toFloat() / 2 - 5, (Gdx.graphics.height).toFloat() / 2, Align.topRight)
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent, x : Float, y : Float) {
                    application.screen = MenuView(application)
                }
            })
        }

        // add all actors to stage
        stage.addActor(lblGameOver)
        stage.addActor(btnReplay)
        stage.addActor(btnBackToMenu)

        return stage
    }

    /**
     * Display a dialog in the bottom of the screen. Game input is stopped and only continued by verifying via onResultReceived().
     *
     * @param dialog dialog object to display
     *
     * @author Markus Thielker
     *
     * */
    fun showDialog(dialog : Dialog) {

        // pause game (stop input | no dialog)
        gamePaused = true
        btnGamePause.isVisible = false

        // show dialog
        dialog.show(activeStage)

        // resize dialog size
        dialog.height = 60f
        dialog.width = 400f

        // reposition dialog
        dialog.setPosition((Gdx.graphics.width / 2).toFloat(), 40f, Align.center)
    }

    /**
     * Passes the messages content to parent class.
     *
     * @param title title of the dialog
     * @param message message of the dialog
     *
     * @author Markus Thielker
     *
     * */
    fun sendMessage(title : String, message : String) {
        showMessage(activeStage, title, message, application.skin)
    }

    /**
     * Verify that a previously displayed dialog has been closed.
     *
     * @author Markus Thielker
     *
     * */
    fun onResultReceived() {

        // reactivate input when dialog result received
        gamePaused = !gamePaused
        btnGamePause.isVisible = !btnGamePause.isVisible
    }

    /**
     * This function gets called to pause the game and show the pause dialog.
     *
     * @author Markus Thielker
     *
     * */
    private fun switchPause() {

        // pause game (stop input) and show dialog
        gamePaused = !gamePaused
        btnGamePause.isVisible = !btnGamePause.isVisible
        dialogPause.isVisible = gamePaused
    }
}