package co.example.michael.uploadpicturetest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by Michael on 2015/1/26.
 */
public class UploadPictureTask extends AsyncTask<ParamsBean, Integer, Void> {

    //边界前缀
    private final String PREFIX = "--";

    //后缀吧
    private final String END_LINE = "\r\n";

    //随机生成边界标示
    private final String BOUNDARYSTR = UUID.randomUUID().toString();

    //文件的键
    private String fileKey = "";

    //文件对象
    private File file;

    //上传文件的类型，如：图片-"image/*" 、"image/png"
    private String fileType = "";

    //上传的内容
    private String sendContent = "";

    //上传内容的长度,字符串
    private String contentLength;

    //上传内容的长度，整型
    private int contentLengthInt;

    private int totalFileBytes;

    private String uploadUrlStr;

    private Context context;

    private HttpURLConnection httpURLConnection = null;

    private ProgressDialog progressDialog;

    private OnUploadListener listener;

    private ParamsBean bean;

    private static UploadPictureTask task;

    public UploadPictureTask() {
    }

    public UploadPictureTask(Context context)
    {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    /**
     * 设置接口的对象
     * @param listener
     */
    public void setOnUploadListener(OnUploadListener listener){
        this.listener = listener;
    }

    /**
     * 单例模式获取该异步任务的对象
     * @return
     */
    public static UploadPictureTask getInstance(){
        if (task == null){
            task = new UploadPictureTask();
            return task;
        }

        return task;
    }


    @Override
    protected void onPreExecute() {

        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setMessage("Image downloading ... %0");
        progressDialog.show();

        Log.i("UI thread", " >> onPreExecute()");

//        listener.onPrepare();
    }

    @Override
    protected Void doInBackground(ParamsBean... params) {

        bean = params[0];
        //获得文件的键
        fileKey = params[0].getFileKey();

        if (params[0].getFilePath() == null || params[0].getFilePath().equals("")) {
            Log.e("UploadPictureTask:", "文件路径为空");
        }

        //根据文件路径获得文件的对象
        file = new File(params[0].getFilePath());

        //得到文件的总的大小
        totalFileBytes = (int) file.length();

        //获得上传文件的类型
        fileType = params[0].getFileType();

        //获得上传url的字符串
        uploadUrlStr = params[0].getUploadUrlStr();

        HashMap<String, String> param = new HashMap<>();

        //将除图片(文件)外的参数通过键值对的形式保存在param中
        param.put("access_token", params[0].getToken());
        param.put("status", params[0].getContent());

        try {
            //构造边界: "--BOUNDARYSTR--\r\n"
            byte[] barry = (PREFIX + BOUNDARYSTR + PREFIX + END_LINE).getBytes("UTF-8");

            //获得发送内容
            sendContent = getMessageParam(param);

            contentLengthInt = sendContent.getBytes("UTF-8").length + (int) file.length() + 2 * barry.length;

            //获得上传内容的长度，字符串类型
            contentLength = Integer.toString(contentLengthInt);

            URL uploadUrl = new URL(uploadUrlStr);

            httpURLConnection = (HttpURLConnection) uploadUrl.openConnection();

            httpURLConnection.setDoInput(true);

            httpURLConnection.setDoOutput(true);

            httpURLConnection.setRequestMethod("POST");

            httpURLConnection.setReadTimeout(10 * 1000);

            httpURLConnection.setConnectTimeout(10 * 1000);

            httpURLConnection.setUseCaches(false);

            //设置一些请求头里的一些属性
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive"); //长链接

            //设置编码
            httpURLConnection.setRequestProperty("Charset", "UTF-8");

            httpURLConnection.setRequestProperty("Content-type", "multipart/form-data;boundary=" + BOUNDARYSTR);

            //内容长度
            httpURLConnection.setRequestProperty("Content-length", contentLength);

            //设置流的固定长度,采用流式的输出,避免出现OutOfMemoryError的错误了
            httpURLConnection.setFixedLengthStreamingMode(contentLengthInt);

            httpURLConnection.connect();

            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());

            //将内容写进数据输出流
            dataOutputStream.write(sendContent.getBytes("UTF-8"));

            //将上传的文件写入输入流中
            FileInputStream fileInputStream = new FileInputStream(file);

            //字节的最大值，保证每次上传的文件不超过1M
            int maxByteSize = 1 * 1024;

            //得到可读的数据的大小
            int dataBytesSize = fileInputStream.available();

            //得到较小的值
            int byteSize = Math.min(maxByteSize, dataBytesSize);

            //新建一个字节数组
            byte[] buffer = new byte[byteSize];

            //从输入流中
            int byteRead = fileInputStream.read(buffer, 0, byteSize);

            //记录当前上传了的文件的大小
            int current = 0;

            while (byteRead > 0) {

                current += byteSize;

                //保证每次上传的内容不超过1M
                dataOutputStream.write(buffer, 0, byteSize);

                //通知进度条更新
                publishProgress(current);
//                listener.onUploading(current);

                dataBytesSize = fileInputStream.available();

                byteSize = Math.min(maxByteSize, dataBytesSize);

                byteRead = fileInputStream.read(buffer, 0, byteSize);

                dataOutputStream.flush();
            }

//            publishProgress(current);

              //第二种上传图片的方法：直接把图片转化成字节类型，然后上传
//            dataOutputStream.write(bean.getPictureBytes());

            dataOutputStream.flush();

            dataOutputStream.write(barry);

            dataOutputStream.write(barry);

            dataOutputStream.flush();

            dataOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int status = httpURLConnection.getResponseCode();

            Log.e("状态码：", "" + status);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Log.i("UI thread", " >> onProgressUpdate() %" + values[0]);
        progressDialog.setMessage("Image downloading ... %" + values[0]);
        progressDialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
//        listener.onComplete();
        super.onPostExecute(aVoid);
    }

    /**
     * 构造上传内容，并返回
     *
     * @param param
     * @return
     */
    private String getMessageParam(HashMap<String, String> param) {
        StringBuffer buffer = new StringBuffer();

        //内容边界：--BOUNDARYSTR\r\n
        buffer.append(PREFIX).append(BOUNDARYSTR).append(END_LINE);

        //将哈希表中的键都放入迭代器中
        Iterator keys = param.keySet().iterator();

        //当迭代器中有元素
        while (keys.hasNext()) {

            //获取url参数的键
            String key = (String) keys.next();

            //获取键对应的值
            String value = param.get(key);

            buffer.append("Content-Disposition:form-data;name=\"") //双引号：name="";
                    .append(key).append("\"").append(END_LINE).append(END_LINE) //两个回车
                    .append(value).append(END_LINE)  //输入键所对应的值
                    .append(PREFIX).append(BOUNDARYSTR).append(END_LINE); //每个参数的结束边界
        } // 迭代添加url参数结束，可能会问此循环里的第一个添加的数怎么是keys.next(): 其实我也不是清楚，终止肯定都添加了

        //添加图片（文件）
        buffer.append("Content-Disposition:form-data;name=\"")
                .append(fileKey).append("\"").append(";")
                .append("filename=\"").append(file.getName()).append("\"")
                .append(END_LINE)
                .append("Content-Type:").append(fileType).append(END_LINE).append(END_LINE);

        //获得上传内容的字符串
        String sendContentStr = buffer.toString();
        return sendContentStr;
    }

    /**
     * 创建一个水平的进度条
     */
    private void setHorizontalDialog() {

        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        progressDialog.setTitle(null);

        progressDialog.setMax(100);

        progressDialog.setProgress(0);

        progressDialog.setMessage("图片正在上传...");

        progressDialog.setIndeterminate(false);

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                httpURLConnection.disconnect();
                dialog.dismiss();
            }
        });

        //设置不可以通过返回按钮退出对话框，默认是true,可以通过返回按钮退出对话框
        progressDialog.setCancelable(false);
    }

    /**
     * 延迟一秒
     */
    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
