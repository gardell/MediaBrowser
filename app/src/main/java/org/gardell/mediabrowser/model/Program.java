package org.gardell.mediabrowser.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;

public final class Program implements Parcelable {
    private final int id;
    @NonNull
    private final URL imageUrl;
    @NonNull
    private final String name;
    @NonNull
    private final String description;

    public Program(
            int id,
            @NonNull URL imageUrl,
            @NonNull String name,
            @NonNull String description) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.description = description;
    }

    private Program(@NonNull Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        try {
            imageUrl = new URL(in.readString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static final Creator<Program> CREATOR = new Creator<Program>() {
        @Override
        public Program createFromParcel(Parcel in) {
            return new Program(in);
        }

        @Override
        public Program[] newArray(int size) {
            return new Program[size];
        }
    };

    public int getId() {
        return id;
    }

    @NonNull
    public URL getImageUrl() {
        return imageUrl;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Program program = (Program) o;

        if (id != program.id) return false;
        if (!imageUrl.equals(program.imageUrl)) return false;
        if (!name.equals(program.name)) return false;
        return description.equals(program.description);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + imageUrl.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(description);
    }
}
