package com.otaku.kickassanime.utils;

import com.google.common.hash.Hashing;

import java.util.List;

public class HashUtils {

    public static <T> long hash64(List<T> data) {
      long hash = 0;
      for (T t : data) {
        hash = 31 * hash + t.hashCode();
      }
      return hash;
    }

    public static int sha256(String data) {
        return Hashing.sha256().hashBytes(data.getBytes()).asInt();
    }
}
