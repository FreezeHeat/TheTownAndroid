package ben_and_asaf_ttp.thetownproject;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.provider.Settings;

import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;
import ben_and_asaf_ttp.thetownproject.shared_resources.Game;
import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GameService extends IntentService {
    private GlobalResources globalResources;
    private Game game;
    private Player player;
    private DataPacket dp;

    public GameService() {
        super("GameService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        while(true){
            dp = ClientConnection.getConnection().receiveDataPacket();
            switch(dp.getCommand()){
                case REFRESH_PLAYERS:

                    //refreshed players - who is alive and who's not

                    break;
                case DAY:

                    //day cycle - change GUI and chat

                    break;

                case NIGHT:

                    //night cycle - change GUI and chat

                    break;
                case SNITCH:

                    //get player - get his role for the SNITCH

                    break;
                case EXECUTE:

                    //player was executed - get datapacket with player who was killed

                    break;
                case PLAYER_JOINED:

                    //get a player and players (alert about the player and set the players)

                    break;
                case PLAYER_LEFT:

                    //get the player, alert about him/her

                    break;
                case READY:

                    //game begun (30 seconds wait and starting DAY phase) - get players

                    break;
                case WIN_CITIZENS:

                    //get refreshed players with stats and game history - check each one for this player

                    break;
                case WIN_KILLERS:

                    //get refreshed players with stats and game history - check each one for this player

                    break;
                case SERVER_SHUTDOWN:

                    //server shuts down...

                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        globalResources = (GlobalResources)getApplication();
        game = globalResources.getGame();
        player = globalResources.getPlayer();
        dp = new DataPacket();
    }
}
