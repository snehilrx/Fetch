package com.otaku.kickassanime.ui;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseIntArray;
import androidx.annotation.NonNull;

public class ParcelableSparseIntArray extends SparseIntArray implements Parcelable {

    public static final Creator<ParcelableSparseIntArray> CREATOR =
        new Creator<>() {
          @NonNull
          @Override
          public ParcelableSparseIntArray createFromParcel(@NonNull Parcel source) {
            int size = source.readInt();
            ParcelableSparseIntArray read = new ParcelableSparseIntArray(size);

            int[] keys = new int[size];
            int[] values = new int[size];

            source.readIntArray(keys);
            source.readIntArray(values);

            for (int i = 0; i < size; i++) {
              read.put(keys[i], values[i]);
            }

            return read;
          }

          @NonNull
          @Override
          public ParcelableSparseIntArray[] newArray(int size) {
            return new ParcelableSparseIntArray[size];
          }
        };

  public ParcelableSparseIntArray(int initialCapacity) {
    super(initialCapacity);
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    int[] keys = new int[size()];
    int[] values = new int[size()];

    for (int i = 0; i < size(); i++) {
      keys[i] = keyAt(i);
      values[i] = valueAt(i);
    }

    dest.writeInt(size());
    dest.writeIntArray(keys);
    dest.writeIntArray(values);
  }

  @Override
  public int describeContents() {
    return 0;
  }
}
