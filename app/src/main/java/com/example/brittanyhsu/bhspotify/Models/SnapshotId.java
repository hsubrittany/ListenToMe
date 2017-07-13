package com.example.brittanyhsu.bhspotify.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by brittanyhsu on 7/12/17.
 */
public class SnapshotId {

    @SerializedName("snapshot_id")
    @Expose
    private String snapshotId;

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

}