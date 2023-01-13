package org.example.model;

import com.google.gson.annotations.SerializedName;

public class VideoInfo {
    @SerializedName("id")
    public int Id;
    @SerializedName("md5")
    public String Md5;
    @SerializedName("link")
    public String Link;

    @SerializedName("cover")
    public String Cover;
    @SerializedName("size")
    public int Size;
    @SerializedName("title")
    public String Title;
    @SerializedName("description")
    public String Description;
    @SerializedName("hashTags")
    public String[] HashTags;
    @SerializedName("topics")
    public String[] Topics;
    @SerializedName("created_time")
    public int CreatedTime;
}