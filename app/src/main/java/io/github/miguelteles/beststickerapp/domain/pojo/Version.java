package io.github.miguelteles.beststickerapp.domain.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class Version implements Parcelable {

    private String version;
    private String message;
    private boolean isUpdateOptional;
    private List<String> changes;

    public Version() {
        //precisa para o Gson
    }

    protected Version(Parcel in) {
        version = in.readString();
        message = in.readString();
        isUpdateOptional = in.readByte() != 0;
        changes = in.createStringArrayList();
    }

    public static final Creator<Version> CREATOR = new Creator<Version>() {
        @Override
        public Version createFromParcel(Parcel in) {
            return new Version(in);
        }

        @Override
        public Version[] newArray(int size) {
            return new Version[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(version);
        dest.writeString(message);
        dest.writeByte((byte) (isUpdateOptional ? 1 : 0));
        dest.writeStringList(changes);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isUpdateOptional() {
        return isUpdateOptional;
    }

    public void setUpdateOptional(boolean updateOptional) {
        isUpdateOptional = updateOptional;
    }

    public List<String> getChanges() {
        return changes;
    }

    public void setChanges(List<String> changes) {
        this.changes = changes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
