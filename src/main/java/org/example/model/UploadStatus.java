package org.example.model;

import com.google.gson.annotations.SerializedName;

public class UploadStatus {
    @SerializedName("md5")
    public String Md5;

    @SerializedName("uploaded_chunks")
    public int[] UploadedChunks;

    @SerializedName("uploaded_cover")
    public Boolean UploadedCover;
    @SerializedName("status")
    public int Status;
}
