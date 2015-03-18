package co.example.michael.uploadpicturetest;

/**
 * Created by Michael on 2015/1/28.
 */
public interface OnUploadListener {

    //准备上传，显示进度条
    public void onPrepare();

    //正在上传
    public void onUploading(int progress);

    //上传完成
    public void onComplete();

}
