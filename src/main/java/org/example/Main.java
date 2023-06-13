package org.example;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.example.model.HttpResponse;
import org.example.model.MetaDataVideo;
import org.example.model.UploadStatus;
import org.example.model.VideoInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Main {
    Logger logger = LoggerFactory.getLogger(Main.class);
    public static final int ChunkSize = 512 * 1024;
    public static final String baseURL = "http://nft.skymeta.pro/pose_dance_api/v1_0";
//    public static final String baseURL = "http://127.0.0.1:9081/pose_dance_api/v1_0";

    public static String getMd5(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.reset();
        md.update(data);
        byte[] digest = md.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashtext = bigInt.toString(16);
        // Now we need to zero pad it if you actually want the full 32 chars.
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext.toLowerCase();
    }

    public static UploadStatus initUploadVideo(String md5video, String md5Cover, int size) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        String uri = baseURL + "/videos/upload/" + md5video + "/" + md5Cover + "/" + size + "/init";
        System.out.println(uri);
        HttpPost httpPost = new HttpPost(uri);
        CloseableHttpResponse response = client.execute(httpPost);
        String json = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
        System.out.println("response " + json);
        client.close();

        HttpResponse dataResponse = new HttpResponse();
        dataResponse = new Gson().fromJson(json, dataResponse.getClass());
        dataResponse.Data = new UploadStatus();
        JSONObject jsonObject = new JSONObject(json);
        String dataString = jsonObject.getJSONObject("data").toString();
        dataResponse.Data = new Gson().fromJson(dataString, dataResponse.Data.getClass());

        return (UploadStatus) dataResponse.Data;
    }

    public static HttpResponse UploadVideoCover(String md5video, byte[] videoCover) throws Exception {
        String md5Cover = getMd5(videoCover);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(baseURL + "/videos/upload/" + md5video + "/cover?md5=" + md5Cover);
        ByteArrayEntity entity = new ByteArrayEntity(videoCover, ContentType.DEFAULT_BINARY);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = client.execute(httpPost);
        String json = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
        client.close();
        HttpResponse dataResponse = new HttpResponse();
        new Gson().fromJson(json, dataResponse.getClass());
        return dataResponse;
    }

    public static HttpResponse UploadVideoChunk(String md5video, byte[] videoData, int chunkId) throws Exception {
        int offset = chunkId * ChunkSize;
        int len = ChunkSize;
        if (offset + len > videoData.length) {
            len = videoData.length - offset;
        }
        CloseableHttpClient client = HttpClients.createDefault();
        String md5Chunk = getMd5(Arrays.copyOfRange(videoData, offset, offset + len));
        HttpPost httpPost = new HttpPost(baseURL + "/videos/upload/" + md5video + "/chunk/" + chunkId + "?md5=" + md5Chunk);
        ByteArrayEntity entity = new ByteArrayEntity(videoData, offset, len, ContentType.DEFAULT_BINARY);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = client.execute(httpPost);
        String json = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
        client.close();
        HttpResponse dataResponse = new HttpResponse();
        new Gson().fromJson(json, dataResponse.getClass());
        return dataResponse;
    }


    public static HttpResponse FinishUploadVideo(String md5video, MetaDataVideo metaDataVideo) throws Exception {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(baseURL + "/videos/upload/" + md5video + "/finish");
        String data = new Gson().toJson(metaDataVideo);

        StringEntity entity = new StringEntity(data);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = client.execute(httpPost);
        String json = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
        client.close();
        HttpResponse dataResponse = new HttpResponse();
        dataResponse.Data = new VideoInfo();
        new Gson().fromJson(json, dataResponse.getClass());
        return dataResponse;
    }

    public static void UploadMultipartFile() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(baseURL + "/match_results/upload/multipart");

        String coverFile = "/home/tamnb/Pictures/video-capture-5772.png";
        String videoFile = "/home/tamnb/Pictures/download.mp4";

        File cover = new File(coverFile);
        File video = new File(videoFile);

        StringBody playInfo = new StringBody("{\"a\":\"1111\"}", ContentType.TEXT_PLAIN);
        StringBody playTime = new StringBody("1111111111", ContentType.TEXT_PLAIN);

        StringBody deviceId = new StringBody("111111AAAAAAAAA", ContentType.TEXT_PLAIN);
        StringBody score = new StringBody("88888", ContentType.TEXT_PLAIN);
        StringBody userId = new StringBody("1", ContentType.TEXT_PLAIN);
        StringBody audioId = new StringBody("7132058633684944897", ContentType.TEXT_PLAIN);

        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("playInfo", playInfo)
                .addPart("playTime", playTime)
                .addBinaryBody("cover", cover)
                .addBinaryBody("video", video)
                .addPart("device_id", deviceId)
                .addPart("score", score)
                .addPart("user_id", userId)
                .addPart("audio_id", audioId)
                .build();
        httppost.setEntity(reqEntity);

        CloseableHttpResponse response = client.execute(httppost);
        String json = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
        System.out.println(json);
        client.close();
        HttpResponse dataResponse = new Gson().fromJson(json, HttpResponse.class);
        System.out.println(dataResponse.Code);
        System.out.println(dataResponse.Msg);

        dataResponse.Data = new UploadStatus();
        JSONObject jsonObject = new JSONObject(json);
        String dataString = jsonObject.getJSONObject("data").toString();
        dataResponse.Data = new Gson().fromJson(dataString, dataResponse.Data.getClass());

        System.out.println(dataResponse.Data);
    }


    public static void signUpNewUserMultipart() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(baseURL + "/users/signup/multipart");

        String avatarFile = "/home/tamnb/Pictures/avatar.png";


        StringBody name = new StringBody("Alex", ContentType.TEXT_PLAIN);
        StringBody pushToken = new StringBody("0x1saeaeaeANADASDASDAd0x", ContentType.TEXT_PLAIN);
        StringBody deviceId = new StringBody("222222BBBBBB", ContentType.TEXT_PLAIN);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .addPart("name", name)
                .addPart("push_token", pushToken)
                .addPart("device_id", deviceId);
        if (avatarFile.length() > 0) {
            File avatar = new File(avatarFile);
            builder = builder.addBinaryBody("avatar", avatar);
        }
        HttpEntity reqEntity = builder.build();


        httppost.setEntity(reqEntity);

        CloseableHttpResponse response = client.execute(httppost);
        String json = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
        System.out.println(json);
        client.close();
        HttpResponse dataResponse = new Gson().fromJson(json, HttpResponse.class);
        System.out.println(dataResponse.Code);
        System.out.println(dataResponse.Msg);
    }

    public static void SplittingFileAndUpload() throws Exception {
        String coverFile = "/home/tamnb/Pictures/video-capture-5772.png";
        byte[] coverData = Files.readAllBytes(Paths.get(coverFile));

        String videoFile = "/home/tamnb/Projects/src/github.com/princeofthesky/upload_meme_video/ArFace-1673229411752.mp4";
        byte[] videoData = Files.readAllBytes(Paths.get(videoFile));

        int chunkLength = videoData.length / ChunkSize;
        if (videoData.length % ChunkSize > 0) {
            chunkLength = chunkLength + 1;
        }
        String md5Video = getMd5(videoData);
        String md5Cover = getMd5(coverData);
        UploadStatus uploadStatus = initUploadVideo(md5Video, md5Cover, videoData.length);

        if (uploadStatus.Status == 0) {
            System.out.println("Video Exist ! Not upload again ");
        }

        if (!uploadStatus.UploadedCover) {
            UploadVideoCover(md5Video, coverData);
        }
        Map<Integer, Boolean> chunkNotUploads = new HashMap<>();
        for (int i = 0; i < chunkLength; i++) {
            chunkNotUploads.put(i, true);
        }

        for (int i = 0; i < uploadStatus.UploadedChunks.length; i++) {
            chunkNotUploads.remove(uploadStatus.UploadedChunks[i]);
        }

        for (int chunk : chunkNotUploads.keySet()) {
            System.out.println("Start upload chunk " + chunk);
            UploadVideoChunk(md5Video, videoData, chunk);
        }

        MetaDataVideo metaDataVideo = new MetaDataVideo();
        metaDataVideo.Md5Video = md5Video;
        metaDataVideo.Md5Cover = md5Cover;
        metaDataVideo.Size = videoData.length;
        metaDataVideo.Title = "111111";
        metaDataVideo.Description = "2222";


        HttpResponse response = FinishUploadVideo(md5Video, metaDataVideo);
        System.out.println(response.Data);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Hello world!");
//        signUpNewUserMultipart();
        UploadMultipartFile();
        //SplittingFileAndUpload();

    }
}