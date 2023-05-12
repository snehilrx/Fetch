package com.otaku.kickassanime.utils

import com.google.gson.Gson
import com.otaku.kickassanime.api.model.Maverickki
import org.junit.Assert
import org.junit.Test

class UtilsTest {

    @Test
    fun parseMaverickkiLink() {
        val parseMaverickkiLink = Gson().fromJson("{\"videoId\":\"636288177f3820bb346b1352\",\"name\":\"The Eminence in Shadow_EP5_1080P(HD)\",\"thumbnail\":\"/thumb/afc13bbbaa53adcc899f777f34207c954c0a1482/befcbf9d416c.jpg\",\"timelineThumbnail\":\"/thumb/afc13bbbaa53adcc899f777f34207c954c0a1482/86e8399d5909.vtt\",\"hls\":\"/api/hls/afc13bbbaa53adcc899f777f34207c954c0a1482.m3u8\",\"renditionInProgress\":true,\"subtitles\":[{\"name\":\"English\",\"src\":\"/subtitle/afc13bbbaa53adcc899f777f34207c954c0a1482/en.vtt\"},{\"name\":\"Thai\",\"src\":\"/subtitle/afc13bbbaa53adcc899f777f34207c954c0a1482/th.vtt\"},{\"name\":\"Vietnamese\",\"src\":\"/subtitle/afc13bbbaa53adcc899f777f34207c954c0a1482/vi.vtt\"},{\"name\":\"Indonesian\",\"src\":\"/subtitle/afc13bbbaa53adcc899f777f34207c954c0a1482/id.vtt\"},{\"name\":\"Malay\",\"src\":\"/subtitle/afc13bbbaa53adcc899f777f34207c954c0a1482/ms.vtt\"}]}", Maverickki::class.java)
        Assert.assertNotNull("maverikki parsed object is null", parseMaverickkiLink)
    }
}