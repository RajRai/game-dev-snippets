package server.broadcasts;

import server.dao.database.WorkerJobs;
import server.job.CreateNewBroadcast;
import server.job.UpdateBroadcast;

public class Broadcast {

    public static final long NEXT_SERVER_RESTART = 1000; // min value for timestamps. see Misc.clampLongToTimestamp

    private int broadcastId;
    private String message;
    private String url;
    private long endTime;
    private long startTime;
    private long durationMs;

    public Broadcast() {
        WorkerJobs.getInstance().addJob(new CreateNewBroadcast(this));
    }

    /**
     * For DB reads only
     */
    public Broadcast(BroadcastData data) {
        broadcastId = data.broadcastId;
        message = data.message;
        url = data.url;
        endTime = data.endTime;
        startTime = data.startTime;
        durationMs = data.durationMs;
    }

    private void update(){
        WorkerJobs.getInstance().addJob(new UpdateBroadcast(this));
    }

    public int broadcastId() {
        return broadcastId;
    }

    public Broadcast broadcastId(int broadcastId) {
        this.broadcastId = broadcastId;
        return this;
    }

    public String message() {
        return message;
    }

    public Broadcast message(String message) {
        this.message = message;
        update();
        return this;
    }

    public String url() {
        return url;
    }

    public Broadcast url(String url) {
        this.url = url;
        update();
        return this;
    }

    public long endTime() {
        return endTime;
    }

    public Broadcast endTime(long endTime) {
        this.endTime = endTime;
        update();
        return this;
    }

    public long startTime() {
        return startTime;
    }

    public Broadcast startTime(long startTime) {
        this.startTime = startTime;
        update();
        return this;
    }

    public long durationMs() {
        return durationMs;
    }

    public Broadcast durationMs(long durationMs) {
        this.durationMs = durationMs;
        update();
        return this;
    }

    public record BroadcastData(int broadcastId, String message, String url, long startTime, long endTime, long durationMs, boolean isActive){}

}
