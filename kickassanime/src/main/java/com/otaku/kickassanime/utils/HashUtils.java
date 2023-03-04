package com.otaku.kickassanime.utils;

import com.google.common.hash.Hashing;
import com.otaku.kickassanime.api.model.Anime;

import java.util.List;

public class HashUtils {

    public static long hash64(List<Anime> data) {
        long hash = 0;
        for (Anime anime : data) {
            hash = 31*hash + anime.hashCode();
        }
        return hash;
    }

    public static int sha256(String data) {
        return Hashing.sha256().hashBytes(data.getBytes()).asInt();
    }
}
