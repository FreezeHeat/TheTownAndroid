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

        EditText editUser = (EditText)findViewById(R.id.register_editUser);
        EditText editPassword = (EditText)findViewById(R.id.register_editPass);
        EditText editPassword2 = (EditText)findViewById(R.id.register_editRePass);
        EditText editEmail = (EditText)findViewById(R.id.register_txtEmail2);
        TextView confText = (TextView)findViewById(R.id.register_txtEmailResponse);
        Button confirm = (Button) findViewById(R.id.register_btnConfirm);
        Button clear = (Button) findViewById(R.id.register_btnConfirm);
        Button back = (Button) findViewById(R.id.register_btnConfirm);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_btnConfirm:

                break;
            case R.id.register_btnClear:

                break;
            default:
                break;
        }
    }
}
