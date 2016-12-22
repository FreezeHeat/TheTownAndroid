package ben_and_asaf_ttp.thetownproject;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        final TextView txtPlayer = (TextView) findViewById(R.id.stats_PlayerName);
        final TextView statsKills = (TextView) findViewById(R.id.stats_Kills);
        final TextView statsHeals = (TextView) findViewById(R.id.stats_Heals);
        final TextView statsRating = (TextView) findViewById(R.id.stats_Rating);
        final TextView statsWon = (TextView) findViewById(R.id.stats_Wins);
        final TextView statsLost = (TextView) findViewById(R.id.stats_Lost);

        Player p = ((GlobalResources)getApplication()).getStatsPlayer();
        txtPlayer.setText(p.getUsername());
        statsWon.setText(String.valueOf(p.getStats().getWon()));
        statsLost.setText(String.valueOf(p.getStats().getLost()));
        statsKills.setText(String.valueOf(p.getStats().getKills()));
        statsHeals.setText(String.valueOf(p.getStats().getHeals()));
        statsRating.setText(String.valueOf(p.getStats().getRating()));
    }
}
