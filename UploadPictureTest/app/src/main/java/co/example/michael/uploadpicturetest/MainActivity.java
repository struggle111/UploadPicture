package co.example.michael.uploadpicturetest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 相册中选取图片  拍照  上传单张图片  上传进度 图片压缩 创建文件夹及文件等等
 */
public class MainActivity extends Activity implements View.OnClickListener, OnUploadListener {

    private Button selectButton;

    private Button uploadButton;

    private ImageView imageView;

    private Bitmap uploadPicture;

    //启动SelectActivity的返回码
    public static final int INTENT_SELECT = 1;

    private String picturePath = "";

    public static final String UPLOAD_URL = "https://upload.api.weibo.com/2/statuses/upload.json";

    private GridView gridView;

    private GridViewAdapter adapter;

    private ArrayList<Bitmap> bitmapLists;

    private int imageViewWidth;

    private ProgressDialog progressDialog;

    private UploadPictureTask task;

    private final int PREPARE = 1;

    private final int ON_UPLOAD = 2;

    private final int COMPLETE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newFolder("uploadPictureTest");

        initView();
    }

    private void initView() {
        selectButton = (Button) findViewById(R.id.select_button);
        selectButton.setOnClickListener(this);
        uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.main_image_view);

        gridView = (GridView) findViewById(R.id.grid_view);

        bitmapLists = new ArrayList<>();
        Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.add_picture);
        bitmapLists.add(defaultBitmap);
        adapter = new GridViewAdapter(MainActivity.this, bitmapLists);
        gridView.setAdapter(adapter);

        /**
         * 为GridView中的最后一个图片添加点击事件
         */
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "size=" + bitmapLists.size() + ";position=" + position + ";id=" + id, Toast.LENGTH_SHORT).show();

                //bitmapLists是GridView的数据源，判断当当前Item是GridViews的最后一个ImageView时，就触发此事件
                if ((bitmapLists.size() - 1) == position) {

                    //SelectActivity是一个Dialog的主题，可以详细看下它的Style
                    Intent intent = new Intent(MainActivity.this, SelectActivity.class);
                    startActivityForResult(intent, INTENT_SELECT);
                }
            }
        });

        //图片的宽度,缩放后的
        imageViewWidth = defaultBitmap.getWidth();

        //这是单例模式获取对象
        task = UploadPictureTask.getInstance();

        //设置接口
        task.setOnUploadListener(this);

        //设置进度条的一些属性
        setHorizontalDialog();

    }


    /**
     * 处理进度条的一些变化等，主要是上传图片时返回一些数据
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PREPARE: //进度条准备
                    progressDialog.setProgress(50);
                    progressDialog.show();
                    break;
                case ON_UPLOAD: //更新进度条的值
                    int progress = msg.arg1;
                    progressDialog.setProgress(progress);
                    break;
                case COMPLETE: //上传图片完成
                    progressDialog.dismiss();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_button:
                Intent intent = new Intent(MainActivity.this, SelectActivity.class);
                startActivityForResult(intent, INTENT_SELECT);
                break;
            case R.id.upload_button:
                ParamsBean bean = new ParamsBean();
                bean.setContent("tttttttt");
                bean.setFileKey("pic");
                bean.setFilePath(picturePath);
                bean.setFileType("image");
                bean.setToken("2.00UZLiWD0sfgia4e16d208e80gcGFt");
                bean.setUploadUrlStr(UPLOAD_URL);
                bean.setPictureBytes(getBytes(uploadPicture));

//                new UploadPictureTask(MainActivity.this).execute(bean);

//                task.execute(bean);

                UploadPictureTask task =new UploadPictureTask(v.getContext());
                task.execute(bean);
/*                LoadImageTask task = new LoadImageTask(v.getContext());
                task.execute("test");*/

//                bean.setPictureBytes(getBytes(uploadPicture));

//                new TTUploadImageTask().execute(bean);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case INTENT_SELECT:
                    Uri uri = data.getData();
                    uploadPicture = decodeUriAsBitmap(uri);
//                    byte[] bs = getBytes(uploadPicture);
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(bs,0,bs.length);

                    picturePath = decodeUriAsPath(uri);
                    Bitmap bitmap = getimage(picturePath);
                    imageView.setImageBitmap(bitmap);

                    //确保添加图片到倒数第二个
                    int num = bitmapLists.size() - 1;
                    bitmapLists.add(num, bitmap);

                    adapter.notifyDataSetChanged();

//                    imageView.setImageURI(uri);
                    break;
            }
        }
        // 如果是拍照返回的结果
        else if (resultCode == SelectActivity.RESULT_CAMERA) {

            //这是拍照后图片的保存路径，自定义的
            picturePath = Environment.getExternalStorageDirectory() + "/" + "uploadPictureTest/temp.jpg";

            /**
             * 获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转
             */
            int degree = readPictureDegree(picturePath);
            Bitmap bitmap = getimage(picturePath);
            /**
             * 把图片旋转为正的方向
             */
            bitmap = rotaingImageView(degree, bitmap);

            imageView.setImageBitmap(bitmap);

            int num = bitmapLists.size() - 1;
            bitmapLists.add(num, bitmap);
            adapter.notifyDataSetChanged();
        }

    }

    /**
     * 根据uri获取图片的路径
     * 不能解析自定义的路径的uri(自我的理解)
     * @param uri
     * @return
     */
    private String decodeUriAsPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = managedQuery(uri, projection, null, null, null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String path = cursor.getString(column_index);

        return path;
    }

    /**
     * 从uri中解析图片Bitmap
     *
     * @param uri
     * @return
     */
    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;

        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return bitmap;
    }


    /**
     *
     * @param bitmap
     * @return
     */
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);// 压缩位图
            byte[] bytes = baos.toByteArray();// 创建分配字节数组
            return bytes;
        } catch (Exception e) {
            return null;
        } finally {
            if (null != baos) {
                try {
                    baos.flush();
                    baos.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 新建一个文件夹
     */
    private void newFolder(String name) {

        //判断sd是否可用
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            File sdDir = Environment.getExternalStorageDirectory();

            String path = sdDir.getPath() + "/" + name;

            File file = new File(path);

            if (!file.exists()) {
                file.mkdirs();
            }

        }
    }

    /**
     * 质量压缩方法
     */
    private Bitmap compressQualityImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //100代表不压缩图片，把图片保存到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        int options = 100;

        while (baos.toByteArray().length / 1024 > 500) {

            //清空baos
            baos.reset();

            options -= 10;
            if (options < 0) {
                break;
            }

            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }

        //把压缩后的图片放在inBm中
        ByteArrayInputStream inBm = new ByteArrayInputStream(baos.toByteArray());

        //生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(inBm, null, null);

        return bitmap;
    }

    /**
     * 根据图片路径按比例压缩
     *
     * @param srcPath
     * @return
     */
    private Bitmap getimage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = imageViewWidth;
        float ww = imageViewWidth;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }

        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressQualityImage(bitmap);//压缩好比例大小后再进行质量压缩

        //直接返回，不用质量压缩也可以展示
//        return bitmap;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {

        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();

        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    @Override
    public void onPrepare() {
        Message message = Message.obtain();
        message.what = PREPARE;
        handler.sendMessage(message);
    }

    @Override
    public void onUploading(int progress) {
        Message message = Message.obtain();
        message.what = ON_UPLOAD;
        message.arg1 = progress;
        handler.sendMessage(message);
    }

    @Override
    public void onComplete() {
        Message message = Message.obtain();
        message.what = COMPLETE;
        handler.sendMessage(message);
    }

    /**
     * 创建一个水平的进度条
     */
    private void setHorizontalDialog() {

        progressDialog = new ProgressDialog(this);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        progressDialog.setTitle(null);

        progressDialog.setMax(100);

        progressDialog.setProgress(0);

        progressDialog.setMessage("图片正在上传...");

        progressDialog.setIndeterminate(false);

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "暂停", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "上传图片已暂停", Toast.LENGTH_SHORT).show();
            }
        });

        //设置不可以通过返回按钮退出对话框，默认是true,可以通过返回按钮退出对话框
        progressDialog.setCancelable(false);
    }
}
