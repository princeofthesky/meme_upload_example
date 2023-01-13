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
    public static final String baseURL = "http://nft.skymeta.pro";

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
        HttpPost httppost = new HttpPost(baseURL + "/videos/upload/multipart");

        String coverFile = "/home/tamnb/Pictures/video-capture-5772.png";
        String videoFile = "/home/tamnb/Projects/src/github.com/princeofthesky/upload_meme_video/ArFace-1673229411752.mp4";

        File cover = new File(coverFile);
        File video = new File(videoFile);

        StringBody title = new StringBody("Title 111111 ", ContentType.TEXT_PLAIN);
        StringBody description = new StringBody("Description 111111 ", ContentType.TEXT_PLAIN);

        StringBody hashTags = new StringBody(new Gson().toJson(new String[]{"hash tag 1 ", "hash tag 2", "hash tag 3"}), ContentType.TEXT_PLAIN);
        StringBody topics = new StringBody(new Gson().toJson(new String[]{"topics 1", "topics 2", "topics 3"}), ContentType.TEXT_PLAIN);


        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("title", title)
                .addPart("description", description)
                .addPart("description", description)
                .addPart("hash_tags", hashTags)
                .addPart("topics", topics)
                .addBinaryBody("cover", cover)
                .addBinaryBody("video", video)
                .build();
        httppost.setEntity(reqEntity);

        CloseableHttpResponse response = client.execute(httppost);
        String json = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
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

        UploadMultipartFile();
        //SplittingFileAndUpload();

    }
}