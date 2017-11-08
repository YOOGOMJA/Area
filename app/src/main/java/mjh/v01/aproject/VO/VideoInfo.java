package mjh.v01.aproject.VO;

import android.net.Uri;

/**
 * Created by jihun on 2017-10-02.
 */

public class VideoInfo {
    //video uri
    private String uri;
    //video coordinate
    private int coordinate;

    public VideoInfo() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(int coordinate) {
        this.coordinate = coordinate;
    }

    public VideoInfo(String uri, int coordinate) {
        this.uri = uri;
        this.coordinate = coordinate;
    }
}
