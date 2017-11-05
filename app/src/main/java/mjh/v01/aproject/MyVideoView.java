package mjh.v01.aproject;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import mjh.v01.aproject.VO.VideoInfo;

public class MyVideoView extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static String EXTERNAL_STORAGE_PATH = "";
    private static String RECORDED_FILE = "video_recorded";
    private static int fileIndex = 0;
    private static String filename = "";
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;

    MediaPlayer player;
    MediaRecorder recorder;
    private Camera camera = null;
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
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "External Storage Media is not mounted.");
        } else {
            EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        // create a SurfaceView instance and add it to the layout
        class Preview extends SurfaceView implements SurfaceHolder.Callback {
            SurfaceHolder mHolder;

            Preview(Context context) {
                super(context);

                mHolder = getHolder();
                mHolder.addCallback(this);
                mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }



            public void surfaceCreated(SurfaceHolder holder) {
                // Surface가 생성되었으니 프리뷰를 어디에 띄울지 지정해준다. (holder 로 받은 SurfaceHolder에 뿌려준다.

                //출처: http://rinear.tistory.com/entry/AndroidSurfaceView-Camera-Preview카메라-프리뷰 [괴도군의 블로그]
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                // 프리뷰 제거시 카메라 사용도 끝났다고 간주하여 리소스를 전부 반환한다

//                출처: http://rinear.tistory.com/entry/AndroidSurfaceView-Camera-Preview카메라-프리뷰 [괴도군의 블로그]
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                // 프리뷰를 회전시키거나 변경시 처리를 여기서 해준다.
                // 프리뷰 변경시에는 먼저 프리뷰를 멈춘다음 변경해야한다.


//                출처: http://rinear.tistory.com/entry/AndroidSurfaceView-Camera-Preview카메라-프리뷰 [괴도군의 블로그]
            }
        }

        Preview surface = new Preview(this);
        holder = surface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        frame.addView(surface);


        btnRecord.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (recorder == null) {
                        recorder = new MediaRecorder();
                    }
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
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
                videoUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                        if (videoUri == null) {
                            Log.d("SampleVideoRecorder", "Video insert failed.");
                            Toast.makeText(MyVideoView.this, "fail", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.d("Whatisthis", videoUri.getLastPathSegment());
                        Toast.makeText(MyVideoView.this, "Success", Toast.LENGTH_SHORT).show();
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, videoUri));
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player == null) {
                    player = new MediaPlayer();
                }

                try {
                    player.setDataSource(filename);
                    player.setDisplay(holder);

                    player.prepare();
                    player.start();
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
                //progressDialog를 띄어주고

                Log.d(" tPathSegment()", videoUri.getLastPathSegment());
                StorageReference riversRef = storageRef.child("video/" + email + "/" + videoUri.getLastPathSegment());
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
            Toast.makeText(this, "11111", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "22222", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "33333", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(this, permissions[i] + "44444", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + "55555", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private String createFilename() {
        fileIndex++;
        Date d= new Date();
        String newFilename = "";
        if (EXTERNAL_STORAGE_PATH == null || EXTERNAL_STORAGE_PATH.equals("")) {
            // use internal memory
            newFilename = RECORDED_FILE + d.toString() + ".mp4";
        } else {
            newFilename = EXTERNAL_STORAGE_PATH + "/" + RECORDED_FILE + d.toString() + ".mp4";
        }

        return newFilename;
    }


    protected void onPause() {
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

    private void setCount() {
        databaseReference.child("VIDEO").child(email).child("count").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue(1);

                } else {
                    int count = mutableData.getValue(Integer.class);
                    mutableData.setValue(count + 1);
                    Log.d("PictureAdapter", "groupName : " + count + " Mod:" + String.valueOf(count + 1));
                }
                return Transaction.success(mutableData);
            }

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
        //00�� ��ǥ�ӽ�
        VideoInfo videoInfo = new VideoInfo(videoUri.getLastPathSegment(), 00);
        databaseReference.child("VIDEO").child(email).child(String.valueOf(index)).setValue(videoInfo);
        //��� push����
        Intent intent = new Intent(MyVideoView.this, MainActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

}
