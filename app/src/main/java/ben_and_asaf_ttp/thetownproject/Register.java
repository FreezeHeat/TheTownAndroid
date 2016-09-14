package ben_and_asaf_ttp.thetownproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Register extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText editUser = (EditText)findViewById(R.id.editUser);
        EditText editPassword = (EditText)findViewById(R.id.editPass);
        EditText editPassword2 = (EditText)findViewById(R.id.editRePass);
        EditText editEmail = (EditText)findViewById(R.id.txtEmail2);
        TextView confText = (TextView)findViewById(R.id.txtEmailResponse);
        Button confirm = (Button) findViewById(R.id.btnConfirm);
        Button clear = (Button) findViewById(R.id.btnConfirm);
        Button back = (Button) findViewById(R.id.btnConfirm);



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnConfirm:

                break;

            case R.id.btnClear:

                break;

            case R.id.btnBack:
                // do your code
                break;


            default:
                break;
        }

    }
}
