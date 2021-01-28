package de.markus_thielker.gomoku.views

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
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
    private lateinit var btnGamePause : TextButton
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
    private val cooldownLength : Long = 500

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

        // create game view pause button
        btnGamePause = TextButton("II", application.skin) // TODO: change string to icon
        btnGamePause.setSize(30f, 30f)
        btnGamePause.setPosition((Gdx.graphics.width - 30).toFloat(), (Gdx.graphics.height - 30).toFloat())
        btnGamePause.addListener(object : ClickListener() {
            override fun clicked(event : InputEvent, x : Float, y : Float) {
                switchPause()
            }
        })

        // add widgets to game stage
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

        activeStage.draw()

        if (gameplay.gameOver) {
            activeStage = stageOver
            Gdx.input.inputProcessor = stageOver
            lblGameOver.setText("${gameplay.winnerPlayer.name} has won the game!")
            lblGameOver.setPosition((Gdx.graphics.width).toFloat() / 2 - (lblGameOver.width / 2), (Gdx.graphics.height).toFloat() / 2 - 300)
            gamePaused = true
        }

        if (!gamePaused) {

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

            // rendering preview
            if (minDist < 100) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
                shapeRenderer.color = Color.GREEN
                shapeRenderer.circle((cornerTL + (minX * 37)), Gdx.graphics.height - (padding + (minY * 37)), 7.5f)
                shapeRenderer.end()
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

            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && minDist < 100 && !cooldown) {
                gameplay.stonePlaced(minX, minY)

                GlobalScope.launch {
                    cooldown = true
                    delay(cooldownLength)
                    cooldown = false
                }
            }
        }
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

        // create pause dialog continue button
        btnPauseContinue = TextButton("Continue", application.skin)
        btnPauseContinue.setSize(150f, 30f)
        btnPauseContinue.addListener(object : ClickListener() {
            override fun clicked(event : InputEvent, x : Float, y : Float) {
                switchPause()
            }
        })

        // create pause dialog back button
        btnPauseBack = TextButton("Back to Menu", application.skin)
        btnPauseBack.setSize(150f, 30f)
        btnPauseBack.addListener(object : ClickListener() {
            override fun clicked(event : InputEvent, x : Float, y : Float) {
                application.screen = MenuView(application)
            }
        })

        // add widgets to dialog
        dialog.contentTable.add(lblPauseHeading)
        dialog.contentTable.add(btnPauseContinue)
        dialog.contentTable.add(btnPauseBack)

        return dialog
    }

    private fun setupStageOver() : Stage {

        val stage = Stage()

        lblGameOver = Label("${gameplay.winnerPlayer.name} won the game!", application.skin)
        lblGameOver.setPosition((Gdx.graphics.width).toFloat() / 2, (Gdx.graphics.height).toFloat() / 2, Align.center)

        btnReplay = TextButton("Rematch", application.skin)
        btnReplay.setSize(150f, 30f)
        btnReplay.setPosition((Gdx.graphics.width).toFloat() / 2, (Gdx.graphics.height).toFloat() / 2, Align.topLeft)
        btnReplay.addListener(object : ClickListener() {
            override fun clicked(event : InputEvent, x : Float, y : Float) {
                application.screen = GameView(application, config, gameplay.playerOne, gameplay.playerTwo)
            }
        })

        btnBackToMenu = TextButton("Back to Menu", application.skin)
        btnBackToMenu.setSize(150f, 30f)
        btnBackToMenu.setPosition((Gdx.graphics.width).toFloat() / 2, (Gdx.graphics.height).toFloat() / 2, Align.topRight)
        btnBackToMenu.addListener(object : ClickListener() {
            override fun clicked(event : InputEvent, x : Float, y : Float) {
                application.screen = MenuView(application)
            }
        })

        stage.addActor(lblGameOver)
        stage.addActor(btnReplay)
        stage.addActor(btnBackToMenu)

        return stage
    }

    private fun switchPause() {
        gamePaused = !gamePaused
        dialogPause.isVisible = gamePaused
    }
}