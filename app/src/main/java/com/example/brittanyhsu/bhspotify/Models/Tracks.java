package com.example.brittanyhsu.bhspotify.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

<<<<<<< HEAD
=======
import java.util.List;

>>>>>>> 3bd07be0f7c8b51c2579b84aaf66689b093149df
/**
 * Created by brittanyhsu on 6/27/17.
 */

public class Tracks {
    @SerializedName("href")
    @Expose
    private String href;
    @SerializedName("total")
    @Expose
    private Integer total;


    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

}
