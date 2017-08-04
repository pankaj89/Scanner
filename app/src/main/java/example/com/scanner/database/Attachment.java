package example.com.scanner.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by Pankaj Sharma on 16/6/17.
 */

@Entity
public class Attachment implements Parcelable {
    @PrimaryKey
    public String id;
    @ColumnInfo(name = "path")
    public String path;
    @ColumnInfo(name = "content_type")
    public String contentType;
    @ColumnInfo(name = "local_path")
    public String localPath;
    @ColumnInfo(name = "is_done")
    public boolean isDone;
    @ColumnInfo(name = "inbound")
    public boolean inbound;
    @ColumnInfo(name = "progress")
    public int progress;
    @ColumnInfo(name = "timestamp")
    public long timestamp;

    public Attachment() {

    }

    protected Attachment(Parcel in) {
        id = in.readString();
        path = in.readString();
        contentType = in.readString();
        localPath = in.readString();
        isDone = in.readByte() != 0;
        inbound = in.readByte() != 0;
        progress = in.readInt();
        timestamp = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(path);
        dest.writeString(contentType);
        dest.writeString(localPath);
        dest.writeByte((byte) (isDone ? 1 : 0));
        dest.writeByte((byte) (inbound ? 1 : 0));
        dest.writeInt(progress);
        dest.writeLong(timestamp);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public boolean isInbound() {
        return inbound;
    }

    public void setInbound(boolean inbound) {
        this.inbound = inbound;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
        @Override
        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };

    public boolean isDownloadable() {
        if (!TextUtils.isEmpty(path) && path.startsWith("http") && !path.contains(" ")) {
            return true;
        }
        return false;
    }


}
