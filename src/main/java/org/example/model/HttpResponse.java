package org.example.model;

import com.google.gson.annotations.SerializedName;

public class HttpResponse {
    @SerializedName("msg")
    public String Msg;

    @SerializedName("code")
    public int Code;
    @SerializedName("data")
    public Object Data;
}
