package ben_and_asaf_ttp.thetownproject;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;
import java.util.List;

import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

public class GameGuide extends AppCompatActivity implements View.OnClickListener{
    ShowcaseView showcaseView;
    int counter = 0;
    GridView grid;
    TextView txtGameTimer;
    TextView txtGameChat;
    TextView playerRole;
    TextView txtPlayerName;
    TextView txtPlayerVotes;
    Button btnPlayerAction;
    ImageView imgvGamePhase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        final ArrayList<Player> list = new ArrayList<>();
        list.add(new Player("Dan", "1"));
        list.add(new Player("Garry", "2"));
        list.add(new Player("Sammy", "3"));
        list.add(new Player("Shay", "4"));
        list.get(0).setAlive(true);
        list.get(1).setAlive(false);
        list.get(2).setAlive(true);
        list.get(3).setAlive(false);

        grid = (GridView)findViewById(R.id.game_playerGrid);
        grid.setAdapter(new MyPlayerAdapter(this, R.layout.player_card, list));
        grid.setAlpha(0.4f);
        txtGameTimer = (TextView)findViewById(R.id.game_txt_timer);
        txtGameTimer.setText("28");

        imgvGamePhase = (ImageView)findViewById(R.id.game_imgv_phase);
        imgvGamePhase.setImageResource(R.drawable.night);

        txtGameChat = (TextView)findViewById(R.id.game_chat_txt);
        final String msg = "<font color=\"#009933\">" +
                list.get(0).getUsername() +
                ": </font><font color=\"#ffffff\">" +
                "No way!" +
                "</font><br/>" +
                "<font color=\"#0066ff\">*" + getResources().getString(R.string.game_night_phase) + "*</font><br/>" +
                list.get(2).getUsername() +
                ": </font><font color=\"#009933\">" +
                "Yep, people died" +
                "</font><br/>";

        txtGameChat.setText(Html.fromHtml(msg));

        playerRole = (TextView)findViewById(R.id.gamePlayerRole);
        final String gameRole = String.format(
                getResources().getString(R.string.game_player_role) , getResources().getString(R.string.game_role_killer));
        playerRole.setText(gameRole);

        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(findViewById(R.id.game_chat_txt)))
                .setOnClickListener(this)
                .build();
        showcaseView.setButtonText(getResources().getString(R.string.gameGuide_btn_next));


        grid.setAlpha(0.4f);
        txtGameTimer.setAlpha(0.4f);
        txtGameChat.setAlpha(1f);
        playerRole.setAlpha(0.4f);
        imgvGamePhase.setAlpha(0.4f);

        showcaseView.setContentText(getString(R.string.gameGuide_gameChat));
        showcaseView.setShowcase(new ViewTarget(txtGameChat), true);

    }

    class MyPlayerAdapter extends ArrayAdapter<Player>
    {

        public MyPlayerAdapter(Context context, int resource, List<Player> objects){
            super(context, resource, objects);
        }

        // the method getView is in charge of creating a single line in the list
        // it receives the position (index) of the line to be created
        // the method populates the view with the data from the relevant object (according to the position)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Get the data item for this position
            final Player user = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            // the mechanism recycles objects - so it creates them only the firs time
            // if created already - only update the data inside
            // ( when scrolling)
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.player_card, parent, false);
            }

            txtPlayerName = (TextView) convertView.findViewById(R.id.playerCard_txt_playerUsername);
            btnPlayerAction = (Button) convertView.findViewById(R.id.playerCard_btn_action);
            txtPlayerVotes = (TextView)convertView.findViewById(R.id.playerCard_votes);

            txtPlayerName.setText(user.getUsername());
            if (!user.isAlive()) {
                txtPlayerName.append(" (" + getString(R.string.game_player_alive) + ")");
            }

            txtPlayerVotes.setVisibility(View.VISIBLE);
            switch (position){
                case 0:
                    txtPlayerVotes.setText("3");
                    break;
                case 1:
                    txtPlayerVotes.setVisibility(View.GONE);
                    break;
                case 2:
                    txtPlayerVotes.setText("1");
                    break;
                case 3:
                    txtPlayerVotes.setText("1");
                    break;
            }

            btnPlayerAction.setText(R.string.game_kill);

            return convertView;
        }
    }





    private void setAlpha(float alpha, View... views) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            for (View view : views) {
                view.setAlpha(alpha);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (counter) {
            case 0:

                showcaseView.setShowcase(new ViewTarget(txtGameChat), true);
                break;

            case 1:
                showcaseView.setContentText(getString(R.string.gameGuide_grid1));
                showcaseView.setShowcase(new ViewTarget(grid), true);
                setAlpha(1f,grid);
                setAlpha(0.4f,txtGameTimer,txtGameChat,playerRole,txtPlayerName,btnPlayerAction,imgvGamePhase);
                break;

            case 2:
                showcaseView.setContentText(getString(R.string.gameGuide_grid2));
                break;

            case 3:
                showcaseView.setContentText(getString(R.string.gameGuide_grid3));
                break;

            case 4:
                showcaseView.setContentText(getString(R.string.gameGuide_gamePhase1));
                showcaseView.setShowcase(new ViewTarget(imgvGamePhase), true);
                setAlpha(1f,imgvGamePhase);
                setAlpha(0.4f,grid,txtGameTimer,txtGameChat,playerRole,txtPlayerName,btnPlayerAction);
                break;

            case 5:
                showcaseView.setContentText(getString(R.string.gameGuide_gamePhase2));
                break;

            case 6:
                showcaseView.setContentText(getString(R.string.gameGuide_gamePhase3));
                showcaseView.setShowcase(new ViewTarget(txtGameChat), true);
                setAlpha(1f,txtGameChat);
                setAlpha(0.4f,grid,txtGameTimer,playerRole,txtPlayerName,btnPlayerAction,imgvGamePhase);
                break;

            case 7:
                showcaseView.setContentText(getString(R.string.gameGuide_gamePhase4));
                showcaseView.setShowcase(new ViewTarget(grid), true);
                setAlpha(1f,grid);
                setAlpha(0.4f,txtGameTimer,txtGameChat,playerRole,txtPlayerName,imgvGamePhase);
                break;

            case 8:
                showcaseView.setContentText(getString(R.string.gameGuide_gamePhase5));

                break;

            case 9:
                showcaseView.setContentText(getString(R.string.gameGuide_timer1));
                showcaseView.setShowcase(new ViewTarget(txtGameTimer), true);
                setAlpha(1f,txtGameTimer);
                setAlpha(0.4f,grid,txtGameChat,playerRole,txtPlayerName,btnPlayerAction,imgvGamePhase);
                break;

            case 10:
                showcaseView.setContentText(getString(R.string.gameGuide_timer2));
                break;
            case 11:
                showcaseView.setContentText(getString(R.string.gameGuide_timer3));
                break;
            case 12:
                showcaseView.setContentText(getString(R.string.gameGuide_role1));
                showcaseView.setShowcase(new ViewTarget(grid), true);
                setAlpha(1f,grid);
                setAlpha(0.4f,txtGameTimer,txtGameChat,playerRole,txtPlayerName,btnPlayerAction,imgvGamePhase);
                break;
            case 13:
                showcaseView.setContentText(getString(R.string.gameGuide_role2));
                showcaseView.setShowcase(new ViewTarget(playerRole), true);
                setAlpha(1f,playerRole);
                setAlpha(0.4f,grid,txtGameTimer,txtGameChat,txtPlayerName,btnPlayerAction,imgvGamePhase);
                break;
            case 14:
                showcaseView.setContentText(getString(R.string.gameGuide_role3));

                break;
            case 15:
                showcaseView.setContentText(getString(R.string.gameGuide_role4));

                break;
            case 16:
                showcaseView.setContentText(getString(R.string.gameGuide_role5));

                break;
            case 17:
                showcaseView.setContentText(getString(R.string.gameGuide_role6));

                break;
            case 18:
                showcaseView.setContentText(getString(R.string.gameGuide_role7));

                break;
            case 19:
                showcaseView.setContentText(getString(R.string.gameGuide_role8));
                showcaseView.setShowcase(new ViewTarget(playerRole), true);
                setAlpha(1f,playerRole);
                setAlpha(0.4f,grid,txtGameTimer,txtGameChat,playerRole,txtPlayerName,btnPlayerAction);
                break;
            case 20:
                showcaseView.setTarget(Target.NONE);
                showcaseView.setContentText(getString(R.string.gameGuide_stats1));
                setAlpha(0.1f,grid,txtGameTimer,txtGameChat,playerRole,txtPlayerName,btnPlayerAction);
                break;
            case 21:
                showcaseView.setContentText(getString(R.string.gameGuide_stats2));
                showcaseView.setButtonText(getString(R.string.gameGuide_btn_close));
                break;
            case 22:
                showcaseView.hide();
                finish();
                return;
        }
        counter++;
    }
}
