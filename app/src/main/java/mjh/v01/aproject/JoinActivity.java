package mjh.v01.aproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import mjh.v01.aproject.VO.UserInfo;

public class JoinActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    @BindView(R.id.llfirst)
    LinearLayout llFirst;
    @BindView(R.id.llsecond)
    LinearLayout llSecond;
    FirebaseDatabase firebaseDatabase;
    @BindView(R.id.edtEmail)
    EditText edtEmail;
    @BindView(R.id.edtPasswd)
    EditText edtPasswd;
    @BindView(R.id.edtname)
    EditText edtName;
    @BindView(R.id.edtphone)
    EditText edtPhone;
    @BindView(R.id.edtnick)
    EditText edtNick;
    @BindView(R.id.btnnext)
    Button btnNext;
    @BindView(R.id.btnCreate)
    Button btnCreate;
    @BindView(R.id.btnNickCheck)
    Button btnNickCheck;
    private FirebaseAuth mAuth;
    ProgressDialog progDialog;
    String email;
    String passwd;
    DatabaseReference databaseReference;
    String email_mod;
    int index;
    boolean nickCheck = false;
    ArrayList<String> nicks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        ButterKnife.bind(this);
        getSupportActionBar().hide();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        progDialog = new ProgressDialog(JoinActivity.this);
        mAuth = FirebaseAuth.getInstance();
        nicks = new ArrayList<String>();

        /*if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "Current User:" + mAuth.getCurrentUser().getEmail());
//             Go to Main Page
            GotoMainPage();
        } else {
            Log.d(TAG, "Log out State");
        }*/

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = edtEmail.getText().toString().trim();
                passwd = edtPasswd.getText().toString().trim();
                Log.d(TAG, "Email:" + email + " Password:" + passwd);
             //check email and passwd type
                if (isValidEmail(email) && isValidPasswd(passwd)) {
                    createAccount(email, passwd);
                } else {
                    Toast.makeText(JoinActivity.this,
                            "Check Email or Password",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        btnNickCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check duplicate nicknames in FireBase

                databaseReference.child("USER").child("NICKS").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String message = "You Can't use";
                        if (dataSnapshot.getValue() != null) {

                            ArrayList<String> arr = (ArrayList<String>) dataSnapshot.getValue();

                            for (int i = 0; i < arr.size(); i++) {
                                Log.d("NICKIMSI", arr.get(i) + ":::::MyNick" + edtNick.getText().toString());
                                if (arr.get(i).equals(edtNick.getText().toString().trim())) {
                                    nickCheck = false;
                                    break;
                                } else {
                                    nickCheck = true;
                                }
                            }
                            Log.d("TTAG", nickCheck + "");
                            if (nickCheck == true) {
                                edtNick.setFocusable(false);
                                edtNick.setClickable(false);
                                message = "You Can use";
                            }
                            Toast.makeText(JoinActivity.this,
                                    message, Toast.LENGTH_LONG).show();
                            return;
                        }else{
                            nickCheck=true;
                            if (nickCheck == true) {
                                message = "You Can use";
                            }
                            Toast.makeText(JoinActivity.this,
                                    message, Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        //insert user info to firebase database
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtName.getText().toString().trim();
                String nick = edtNick.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                if (name.length() > 0 && nick.length() > 0 && phone.length() > 0) {
                    if (nickCheck) {
                        UserInfo userInfo = new UserInfo(email, name, nick, passwd, phone);
//                    Map<String, Object> userValues = userInfo.toMap(null);
//                    Map<String, Object> childUpdates = new HashMap<>();
                        getArray(nick);
                        //replacing for save to firebase database
                        email_mod = email.replace(".", "_");
//                    childUpdates.put("/USER/" +email_mod, userValues);
//                    databaseReference.updateChildren(childUpdates);
                        databaseReference.child("USER").child(email_mod).setValue(userInfo);
                        databaseReference.child("VIDEO").child(email_mod).child("count").setValue(0);
                        GotoMainPage();
                    } else {

                        Toast.makeText(JoinActivity.this,
                                "you have to check Nick", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void phoneCheck() {

    }
    //password type
    private boolean isValidPasswd(String str) {
        if (str == null || TextUtils.isEmpty(str)) {
            return false;
        } else {
            if (str.length() > 4)
                return true;
            else
                return false;
        }
    }
    //email type
    private boolean isValidEmail(String str) {
        if (str == null || TextUtils.isEmpty(str)) {
            return false;
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(str).matches();
        }
    }
    //Create an account in FireBase Auth
    private void createAccount(String email, String passwd) {
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setMessage("Create User Account....");
        progDialog.show();
        mAuth.createUserWithEmailAndPassword(email, passwd)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "Create Account:" + task.isSuccessful());
                                progDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Account Create Complete");
                                    Log.d(TAG, "Current User:" + mAuth.getCurrentUser().getEmail());
                                    llFirst.setVisibility(View.INVISIBLE);
                                    llSecond.setVisibility(View.VISIBLE);
                                    // Go go Main
//                                    GotoMainPage();
                                } else {
                                    Toast.makeText(JoinActivity.this,
                                            "Create Account Failed", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
    }

    private void signinAccount(String email, String passwd) {
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setMessage("User Account Log in ....");
        progDialog.show();
        mAuth.signInWithEmailAndPassword(email, passwd)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "Sing in Account:" + task.isSuccessful());
                                progDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Account Log in  Complete");
                                    Log.d(TAG, "Current User:" + mAuth.getCurrentUser().getEmail());
                                    // Go go Main
//                                    GotoMainPage();
                                } else {
                                    Toast.makeText(JoinActivity.this,
                                            "Log In Failed", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

    }
    //finish Join
    private void GotoMainPage() {
        Intent intent = new Intent(JoinActivity.this, MainActivity.class);
        intent.putExtra("email",email);
        startActivity(intent);
        finish();
    }

    //Add a nickname to the database for duplicate nicknames
    private void getArray(final String nick) {
        databaseReference.child("USER").child("NICKS").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    nicks.add(child.getValue(String.class));
                }
                nicks.add(nick);
                Log.d("NICKCHECK", nicks + "");
                databaseReference.child("USER").child("NICKS").setValue(nicks);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}