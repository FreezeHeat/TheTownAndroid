package ben_and_asaf_ttp.thetownproject;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

public class StatsActivity extends AppCompatActivity {
    private TextView txtPlayer;
    private TextView statsKills;
    private TextView statsHeals;
    private TextView statsRating;
    private TextView statsWon;
    private TextView statsLost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        txtPlayer = (TextView)findViewById(R.id.stats_PlayerName);
        statsKills = (TextView)findViewById(R.id.stats_Kills);
        statsHeals = (TextView)findViewById(R.id.stats_Heals);
        statsRating = (TextView)findViewById(R.id.stats_Rating);
        statsWon = (TextView)findViewById(R.id.stats_Wins);
        statsLost = (TextView)findViewById(R.id.stats_Lost);

        Player p = ((GlobalResources)getApplication()).getStatsPlayer();
        this.txtPlayer.setText(p.getUsername());
        this.statsWon.setText(String.valueOf(p.getStats().getWon()));
        this.statsLost.setText(String.valueOf(p.getStats().getLost()));
        this.statsKills.setText(String.valueOf(p.getStats().getKills()));
        this.statsHeals.setText(String.valueOf(p.getStats().getHeals()));
        this.statsRating.setText(String.valueOf(p.getStats().getRating()));
    }
}
