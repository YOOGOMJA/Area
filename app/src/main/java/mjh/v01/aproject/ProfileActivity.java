package mjh.v01.aproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {
    @BindView(R.id.lvVideo)
    ListView lvVideo;
    @BindView(R.id.imgProfile)
    ImageView imgProfile;
    @BindView(R.id.tvNick)
    TextView tvNick;

    String nick;
    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Intent intent = getIntent();
        nick = intent.getStringExtra("nick").toString();
        email = intent.getStringExtra("email");
        ButterKnife.bind(this);
        if(nick.equals(null)){
        }else{
            tvNick.setText(nick);
        }

    }
}
