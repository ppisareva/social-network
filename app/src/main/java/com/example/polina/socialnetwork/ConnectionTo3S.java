package com.example.polina.socialnetwork;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.mobileconnectors.s3.transfermanager.model.UploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.File;
import java.io.InputStream;

/**
 * Created by polina on 04.06.15.
 */
public class ConnectionTo3S {
    private static String MY_ACCESS_KEY_ID = Key.MY_ACCESS_KEY_ID;
    private static String MY_SECRET_KEY = Key.MY_SECRET_KEY;
    private static String BUCKET_NAME = Key.BUCKET_NAME;

   public static String uploadImage(String path) {
        AWSCredentials credentials = new BasicAWSCredentials(MY_ACCESS_KEY_ID, MY_SECRET_KEY);
        TransferManager manager = new TransferManager(credentials);
        Upload upload = manager.upload(BUCKET_NAME, "images/IMG_" + System.currentTimeMillis() + ".png", new File(path));


        try {
            UploadResult r = upload.waitForUploadResult();
            return "https://s3-eu-west-1.amazonaws.com/" + r.getBucketName() + "/" + r.getKey();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String uploadImage(InputStream inputStream) {
        AWSCredentials credentials = new BasicAWSCredentials(MY_ACCESS_KEY_ID, MY_SECRET_KEY);
        TransferManager manager = new TransferManager(credentials);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("jpeg");
        Upload upload = manager.upload(BUCKET_NAME, "images/IMG_" + System.currentTimeMillis() + ".png", inputStream, meta );
        try {
            UploadResult r = upload.waitForUploadResult();


            return "https://s3-eu-west-1.amazonaws.com/" + r.getBucketName() + "/" + r.getKey();


        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }
}
