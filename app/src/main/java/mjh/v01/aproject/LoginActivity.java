package mjh.v01.aproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.btnLogin)
    Button btnLogin;
    @BindView(R.id.btnJoin)
    Button btnJoin;
    @BindView(R.id.edtId)
    EditText edtId;
    @BindView(R.id.edtPw)
    EditText edtPw;
    String id, pw;
    private FirebaseAuth mAuth;
    ProgressDialog progDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        progDialog = new ProgressDialog(LoginActivity.this);

        //check email and passwd in Firebase
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = edtId.getText().toString().trim();
                String pw = edtPw.getText().toString().trim();

                if (isValidEmail(id)) {
                    signin(id, pw);

                } else {
                    Toast.makeText(LoginActivity.this, "Check the Input Data", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //go to join
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("TAG", "눌렀어분명");
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean isValidEmail(String target) {
        if (target == null || TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
    // Login
    private void signin(final String email, String password) {
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setMessage("Log in ....");
        progDialog.show();
        //match checking email and password to firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        progDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login Complete", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("email",email);
                            startActivity(intent);
                            finish();
                        } else {
//                            Log.w(TAG, "signInWithEmail:failed" + task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
