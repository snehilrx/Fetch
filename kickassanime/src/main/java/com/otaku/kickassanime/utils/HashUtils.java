package com.otaku.kickassanime.utils;

import com.google.common.hash.HashCode;

import java.util.List;

public class HashUtils {

  private HashUtils() {
    // no-op
  }

  public static <T> long hash64(List<T> data) {
    long hash = 0;
    for (T t : data) {
      hash = 31 * hash + t.hashCode();
    }
    return hash;
  }

  public static int sha256(String data) {
    return (int) HashCode.fromBytes(data.getBytes()).padToLong();
  }
}
