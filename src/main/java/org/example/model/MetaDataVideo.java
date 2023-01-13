package org.example.model;

import com.google.gson.annotations.SerializedName;

public class MetaDataVideo {


    @SerializedName("md5_video")
    public String Md5Video;

    @SerializedName("md5_cover")
    public String Md5Cover;
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

}
