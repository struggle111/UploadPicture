package co.example.michael.uploadpicturetest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * Created by Michael on 2015/1/26.
 */
public class SelectActivity extends Activity implements View.OnClickListener {

    private Button albumButton;

    private Button cameraButton;

    private Button cancelButton;

    private final int INTENT_ALBUM = 11;

    private final int INTENT_CAMERA = 12;

    public static final int RESULT_CAMERA = 13;

    private String uploadPicturePath = "file:///sdcard/uploadPictureTest/temp.jpg";

    private Uri cameraImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_layout);

        initView();
    }

    private void initView() {
        albumButton = (Button) findViewById(R.id.album_button);
        albumButton.setOnClickListener(this);

        cameraButton = (Button) findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(this);

        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(this);

        cameraImageUri = Uri.parse(uploadPicturePath);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.album_button:
                albumButtonListener();
                break;
            case R.id.camera_button:
                cameraButtonListener();
                break;
            case R.id.cancel_button:
            default:
                finish();
                break;

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }

    /**
     * 打开相册，选取图片
     */
    private void albumButtonListener() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);

        intent.setType("image/*");

        startActivityForResult(intent, INTENT_ALBUM);
    }

    /**
     * 拍照功能
     */
    private void cameraButtonListener() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

        startActivityForResult(intent, INTENT_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case INTENT_ALBUM:
                    setResult(RESULT_OK, data);
                    finish();
                    break;
                case INTENT_CAMERA:
                    Intent intent = new Intent();
                    intent.setData(cameraImageUri);
                    setResult(RESULT_CAMERA, intent);
                    finish();
                    break;
            }
        }
    }
}
