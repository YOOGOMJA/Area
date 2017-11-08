package mjh.v01.aproject;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import butterknife.BindView;
import butterknife.ButterKnife;
import mjh.v01.aproject.VO.VideoInfo;

import static android.view.View.resolveSize;

public class MyVideoView extends AppCompatActivity implements SurfaceHolder.Callback{

    public static final String TAG = "MainActivity";
    private static String EXTERNAL_STORAGE_PATH = "";
    private static String RECORDED_FILE = "video_recorded";
    private static int fileIndex = 0;
    private static String filename = "";
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;

    MediaPlayer player;
    MediaRecorder recorder;
    UploadTask uploadTask;
    SurfaceHolder holder;
    @BindView(R.id.btnRecord)
    Button btnRecord;
    @BindView(R.id.btnRecordStop)
    Button btnRecordStop;
    @BindView(R.id.btnPlay)
    Button btnPlay;
    @BindView(R.id.btnPlayStop)
    Button btnPlayStop;
    @BindView(R.id.btnUpload)
    Button btnUpload;
    @BindView(R.id.videoLayout)
    FrameLayout frame;
    String email;
    int index;
    Uri videoUri;
    Camera mCamera;
    SurfaceView surface;
    public List<Camera.Size> listPreviewSizes;
    private Camera.Size previewSize;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_video_view);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        final StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://aproject-4e6d6.appspot.com/");
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        //Determine whether external memory is available
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "External Storage Media is not mounted.");
        } else {
            EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        //Create a surface view, assign it to the holder, and add it to the frame.
        surface = new SurfaceView(this);
        init();
        /*holder = surface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);*/
        frame.addView(surface);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (recorder == null) {
                        recorder = new MediaRecorder();
                    }
                    //In the recorder, put in the items that I need to put
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
                    recorder.setOrientationHint(90);
                    filename = createFilename();
                    Log.d(TAG, "current filename : " + filename);

                    recorder.setOutputFile(filename);
                    recorder.setPreviewDisplay(holder.getSurface());

                    recorder.prepare();
                    recorder.start();

                } catch (Exception ex) {
                    Log.e(TAG, "Exception : ", ex);

                    recorder.release();
                    recorder = null;
                }

            }
        });

        btnRecordStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (recorder == null)
                    return;
                //Release methods on the recorder
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
                ContentValues values = new ContentValues(10);
                values.put(MediaStore.MediaColumns.TITLE, "RecordedVideo");
                values.put(MediaStore.Audio.Media.ALBUM, "Video Album");
                values.put(MediaStore.Audio.Media.ARTIST, "Mike");
                values.put(MediaStore.Audio.Media.DISPLAY_NAME, "Recorded Video");
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "mp4");
                values.put(MediaStore.Audio.Media.DATA, filename);
                // uri에 인서트 시켜준다
                videoUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                if (videoUri == null) {
                    Log.d("SampleVideoRecorder", "Video insert failed.");
                    Toast.makeText(MyVideoView.this, "fail", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("Whatisthis", videoUri.getLastPathSegment());
                Toast.makeText(MyVideoView.this, "Success", Toast.LENGTH_SHORT).show();
                //미디어 스캐너로 하여금 Uri에 대한 파일을 스캔하고 미디어 라이브러리에 파일을 추가하도록 해줍니다.
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, videoUri));
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player == null) {
                    player = new MediaPlayer();
                }


                try {
                    //경로에 있는 내 파일을 가져와서 실행시킨다
                    FileInputStream fs = new FileInputStream(new File(filename));
                    FileDescriptor fd = fs.getFD();
                    player.setDataSource(fd);
                    player.prepare();

                    Log.d("FileName", filename);
                    player.setDisplay(holder);


                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            player.start();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Video play failed.", e);
                }

            }
        });


        btnPlayStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player == null)
                    return;

                player.stop();
                player.release();
                player = null;
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("UPload", "update setOnClickListener");
                //progressDialog를 띄어줄까 고민중

                Log.d(" tPathSegment()", videoUri.getLastPathSegment());
                //storage path
                StorageReference riversRef = storageRef.child("video/" + email + "/" + videoUri.getLastPathSegment());
                //upload start Fail or Success Listener
                uploadTask = riversRef.putFile(videoUri);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(MyVideoView.this, "Fail", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        setCount();
                    }
                });
            }
        });
        checkDangerousPermissions();
    }


    private void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "11111", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "22222", Toast.LENGTH_SHORT).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "33333", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + "44444", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, permissions[i] + "55555", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String createFilename() {
        Date d = new Date();
        String newFilename = "";
        if (EXTERNAL_STORAGE_PATH == null || EXTERNAL_STORAGE_PATH.equals("")) {
            // use internal memory
            newFilename = RECORDED_FILE + d.toString().trim() + ".mp4";
        } else {
            newFilename = EXTERNAL_STORAGE_PATH + "/" + RECORDED_FILE + d.toString().trim() + ".mp4";
        }

        return newFilename;
    }


    /*protected void onPause() {
        if (camera != null) {
            camera.release();
            camera = null;
        }

        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }

        super.onPause();
    }
*/
    private void setCount() {
        //get data in firebase
        databaseReference.child("VIDEO").child(email).child("count").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                //getValue is my count
                if (mutableData.getValue() == null) {
                    mutableData.setValue(1);

                } else {
                    int count = mutableData.getValue(Integer.class);
                    mutableData.setValue(count + 1);
                    Log.d("PictureAdapter", "groupName : " + count + " Mod:" + String.valueOf(count + 1));
                }
                return Transaction.success(mutableData);
            }
            //This will be called if Transaction.Result doTransaction succeeds.
            @Override
            public void onComplete(DatabaseError databaseError, boolean success, DataSnapshot dataSnapshot) {
                // Analyse databaseError for any error during increment
                int count = dataSnapshot.getValue(Integer.class);
                index = count;
                Log.d("MyTest", "onComplete :" + count);
                Log.d("MyTest", "onComplete Success :" + success);
                updateDB();
            }
        });

    }

    private void updateDB() {
        //update to firebase DB
        VideoInfo videoInfo = new VideoInfo(videoUri.getLastPathSegment(), 00);
        databaseReference.child("VIDEO").child(email).child(String.valueOf(index)).setValue(videoInfo);
        //finish and go to MainActivity
        Intent intent = new Intent(MyVideoView.this, MainActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            // 카메라 객체를 사용할 수 있게 연결한다.
            if(mCamera  == null){
                mCamera  = Camera.open();
            }

            // 카메라 설정
            Camera.Parameters parameters = mCamera .getParameters();

            // 카메라의 회전이 가로/세로일때 화면을 설정한다.
            if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                parameters.set("orientation", "portrait");
                mCamera.setDisplayOrientation(90);
                parameters.setRotation(90);
                Log.d("ifResource","ifResource");
            } else {
                parameters.set("orientation", "landscape");
                mCamera.setDisplayOrientation(0);
                parameters.setRotation(0);
                Log.d("elseResource","elseResource");
            }
            mCamera.setParameters(parameters);

            mCamera.setPreviewDisplay(surfaceHolder);

            // 카메라 미리보기를 시작한다.
            mCamera.startPreview();

            // 자동포커스 설정
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {

                    }
                }
            });
        } catch (IOException e) {
        }
    }
    // SurfaceView 의 크기가 바뀌면 호출
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {

        // 카메라 화면을 회전 할 때의 처리
        if (surfaceHolder.getSurface() == null){
            // 프리뷰가 존재하지 않을때
            return;
        }

    }

    // SurfaceView가 종료시 호출
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(mCamera != null){
            // 카메라 미리보기를 종료한다.
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
    private void init(){
        //Default status camera
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);

        // surfaceview setting
        holder = surface.getHolder();
        holder .addCallback(this);
        holder .setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

}
