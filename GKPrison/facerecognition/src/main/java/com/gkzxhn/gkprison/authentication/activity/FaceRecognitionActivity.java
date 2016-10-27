package com.gkzxhn.gkprison.authentication.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.gkzxhn.gkprison.R;
import com.orhanobut.logger.Logger;

import java.io.File;

/**
 * Created by wrf on 2016/10/26.
 */

public class FaceRecognitionActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 1;
    private Button mButton;
    private FaceSurfaceView mCameraSurfaceView;

    private FrameLayout mFrameLayout;

    private int PICK_IMAGE_REQUEST = 1;

    private File imageFile;

    public static final String FACE_URL = "faceUrl";

    public static final String CONFIDENCE_RESULT = "confidence_result";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.face_recognition_activity);


        mFrameLayout = (FrameLayout) findViewById(R.id.contentview);
        mButton = (Button) findViewById(R.id.button_start_authentication);

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "选择照片"), PICK_IMAGE_REQUEST);
            }
        });

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            requestPermission();
        }else{
            Logger.e("小于23");
            initSurfaceView();
        }


    }

    private void initSurfaceView() {
        mCameraSurfaceView = new FaceSurfaceView(this);
        mCameraSurfaceView.setLayoutParams(
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT
                        , FrameLayout.LayoutParams.MATCH_PARENT));
        mFrameLayout.addView(mCameraSurfaceView, 0);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //                mCameraSurfaceView.setOldFaceFile(new File(getExternalCacheDir() + "/" + "test.jpg"));
//                String faceUrl = getIntent().getStringExtra(FACE_URL);
                String faceUrl = "http://img5.duitang.com/uploads/item/201408/23/20140823145710_iwdLQ.jpeg";
                Logger.e("faceUrl = "+faceUrl);
                if(TextUtils.isEmpty(faceUrl)){
                    setResult(RESULT_CANCELED);
                    onBackPressed();
                    return;
                }

                mCameraSurfaceView.startAuthentication();
                mCameraSurfaceView.setFaceURL(faceUrl);
            }
        });

    }


    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            initSurfaceView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initSurfaceView();
            } else {
                //用户勾选了不再询问
                //提示用户手动打开权限
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "相机权限已被禁止", Toast.LENGTH_SHORT).show();


                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.please_enter_the_authorization);
                    builder.setMessage(R.string.must_get_camera_permission);

                    builder.setPositiveButton("设置授权", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setResult(RESULT_CANCELED);
                            onBackPressed();
                        }
                    });
                    builder.show();
                }
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri uri = data.getData();
                Logger.e("uri= " + uri);
                String path = uri.getPath();
                Logger.e("path= " + path);
                imageFile = new File(path);
                mCameraSurfaceView.setOldFaceFile(imageFile);


            }
        }
    }


}
