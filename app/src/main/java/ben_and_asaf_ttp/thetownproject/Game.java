package ben_and_asaf_ttp.thetownproject;

import java.util.Calendar;

/**
 * Created by Administrator on 13/05/2016.
 */
public class Game {

    String description;
    int numPlayers;

    public Game(String description, int numPlayers) {
        this.numPlayers = numPlayers;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }
}
