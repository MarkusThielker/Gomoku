package de.markus_thielker.gomoku.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import de.markus_thielker.gomoku.Application;

public class DesktopLauncher {

    // window settings
    private static final int client_area_width_ = 1280;
    private static final int client_area_height_ = 720;
    private static final boolean window_resizable_ = false;

    public static void main(String[] arg) {

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        // setting window size to WXGA (HD-ready) 16:9 format
        config.width = client_area_width_;
        config.height = client_area_height_;

        // disable window resizing
        config.resizable = window_resizable_;
        new LwjglApplication(new Application(), config);
    }
}
