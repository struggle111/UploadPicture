package co.example.michael.uploadpicturetest;

import java.util.ArrayList;

/**
 * Created by Michael on 2015/1/26.
 */
public class ParamsBean {

    private String token;

    private String content;

    //文件的url的键
    private String fileKey;

    //文件路径
    private String filePath;

    //文件类型
    private String fileType;

    //上传的地址:url
    private String uploadUrlStr;

    private ArrayList<String> filePathLists;

    public ArrayList<String> getFilePathLists() {
        return filePathLists;
    }

    public void setFilePathLists(ArrayList<String> filePathLists) {
        this.filePathLists = filePathLists;
    }

    private byte[] pictureBytes;

    public byte[] getPictureBytes() {
        return pictureBytes;
    }

    public void setPictureBytes(byte[] pictureBytes) {
        this.pictureBytes = pictureBytes;
    }

    public String getUploadUrlStr() {
        return uploadUrlStr;
    }

    public void setUploadUrlStr(String uploadUrlStr) {
        this.uploadUrlStr = uploadUrlStr;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
