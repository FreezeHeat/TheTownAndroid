package ben_and_asaf_ttp.thetownproject;

import android.app.Application;

import ben_and_asaf_ttp.thetownproject.shared_resources.Game;
import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

public class GlobalResources extends Application {
    private Player player;
    private Player statsPlayer;
    private Game game;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(final Player player) {
        this.player = player;
    }

    public Player getStatsPlayer() {
        return statsPlayer;
    }

    public void setStatsPlayer(final Player statsPlayer) {
        this.statsPlayer = statsPlayer;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(final Game game) {
        this.game = game;
    }
}
