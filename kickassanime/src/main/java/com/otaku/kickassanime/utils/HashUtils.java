package com.otaku.kickassanime.utils;

import com.otaku.kickassanime.api.model.Anime;

import java.util.List;

public class HashUtils {

    public static long hash64(List<Anime> data) {
        long hash = 0xcbf29ce484222325L;
        for (Anime anime : data) {
            hash <<= (anime.hashCode() & 0xff);
            hash *= 16777619;
        }
        return hash;
    }

}
