package de.markus_thielker.gomoku.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
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
import de.markus_thielker.gomoku.components.GomokuConfiguration
import de.markus_thielker.gomoku.components.GomokuFieldColor
import de.markus_thielker.gomoku.components.GomokuGame
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
class GameView(private val application : Application, var config : GomokuConfiguration, playerOne : GomokuPlayer?, playerTwo : GomokuPlayer?) : ScreenAdapter() {

    // game interaction essentials
    private val gameplay = GomokuGame(config, playerOne, playerTwo)
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
    private lateinit var btnPauseBack : TextButton

    // widgets for game over screen
    private lateinit var stageOver : Stage
    private lateinit var lblGameOver : Label
    private lateinit var btnReplay : TextButton
    private lateinit var btnBackToMenu : TextButton

    // grid dimensions
    private val gridSize = 15
    private val padding = 100f
    private val lineWidth = 5f

    // click cooldown
    private var cooldown = false
    private val cooldownLength : Long = 250

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

        // TODO: add turn indicator

        // create player one overview
        lblPlayerNameOne = Label("${gameplay.playerOne!!.name} | Wins: ${gameplay.playerOne!!.wins} | Streak: ${gameplay.playerOne!!.streak}", application.skin)
        lblPlayerNameOne.apply {
            setPosition((Gdx.graphics.width / 2 - 15 - lblPlayerNameOne.width / 2), (Gdx.graphics.height - 50).toFloat(), Align.right)
        }

        // create player two overview
        lblPlayerNameTwo = Label("${gameplay.playerTwo!!.name} | Wins: ${gameplay.playerTwo!!.wins} | Streak: ${gameplay.playerTwo!!.streak}", application.skin)
        lblPlayerNameTwo.apply {
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

    override fun render(delta : Float) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        activeStage.act()

        // draw background to batch
        activeStage.batch.begin()
        activeStage.batch.draw(application.backgroundTexture, 0f, 0f)
        activeStage.batch.draw(application.backgroundTexture, 0f, Gdx.graphics.height.toFloat())
        activeStage.batch.end()

        if (gameplay.gameOver) {
            activeStage = stageOver
            Gdx.input.inputProcessor = stageOver
            lblGameOver.setText("${gameplay.winnerPlayer.name} has won the game!")
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

                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && minDist < 100 && !cooldown) {
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
        }

        activeStage.draw()
    }

    override fun dispose() {
        stageGame.dispose()
    }

    private fun setupPauseDialog() : Dialog {

        // create pause dialog
        val dialog = Dialog("Paused", application.skin)
        dialog.setSize(400f, 200f)
        dialog.setPosition(Gdx.graphics.width.toFloat() / 2 - 200, Gdx.graphics.height.toFloat() / 2 - 100)
        dialog.isVisible = false
        dialog.isMovable = false

        // create pause dialog title label
        lblPauseHeading = Label("Pause", application.skin)
        lblPauseHeading.apply {
            setPosition(dialog.width / 2, dialog.height / 2 + 30, Align.center)
        }

        // create pause dialog continue button
        btnPauseContinue = TextButton("Continue", application.skin)
        btnPauseContinue.apply {
            setSize(150f, 30f)
            setPosition(dialog.width / 2, dialog.height / 2 - 10, Align.center)
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent, x : Float, y : Float) {
                    switchPause()
                }
            })
        }

        // create pause dialog back button
        btnPauseBack = TextButton("Back to Menu", application.skin)
        btnPauseBack.apply {
            setSize(150f, 30f)
            setPosition(dialog.width / 2, dialog.height / 2 - 50, Align.center)
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent, x : Float, y : Float) {
                    application.screen = MenuView(application)
                }
            })
        }

        // add widgets to dialog
        dialog.contentTable.addActor(lblPauseHeading)
        dialog.contentTable.addActor(btnPauseContinue)
        dialog.contentTable.addActor(btnPauseBack)

        return dialog
    }

    private fun setupStageOver() : Stage {

        val stage = Stage()

        lblGameOver = Label("${gameplay.winnerPlayer.name} won the game!", application.skin)
        lblGameOver.apply {
            setPosition((Gdx.graphics.width).toFloat() / 2, (Gdx.graphics.height).toFloat() / 2 + 20, Align.center)
        }

        btnReplay = TextButton("Rematch", application.skin)
        btnReplay.apply {
            setSize(150f, 30f)
            setPosition((Gdx.graphics.width).toFloat() / 2 + 5, (Gdx.graphics.height).toFloat() / 2, Align.topLeft)
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent, x : Float, y : Float) {
                    application.screen = GameView(application, config, gameplay.playerOne, gameplay.playerTwo)
                }
            })
        }

        btnBackToMenu = TextButton("Back to Menu", application.skin)
        btnBackToMenu.apply {
            setSize(150f, 30f)
            setPosition((Gdx.graphics.width).toFloat() / 2 - 5, (Gdx.graphics.height).toFloat() / 2, Align.topRight)
            addListener(object : ClickListener() {
                override fun clicked(event : InputEvent, x : Float, y : Float) {
                    application.screen = MenuView(application)
                }
            })
        }

        stage.addActor(lblGameOver)
        stage.addActor(btnReplay)
        stage.addActor(btnBackToMenu)

        return stage
    }

    private fun switchPause() {
        gamePaused = !gamePaused
        btnGamePause.isVisible = !btnGamePause.isVisible
        dialogPause.isVisible = gamePaused
    }
}