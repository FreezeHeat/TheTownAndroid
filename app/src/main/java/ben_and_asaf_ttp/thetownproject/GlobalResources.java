package ben_and_asaf_ttp.thetownproject;

import android.app.Application;

import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

public class GlobalResources extends Application {
    private Player player;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
