package com.otaku.kickassanime.db.models.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.otaku.kickassanime.Strings.KICKASSANIME_URL
import kotlinx.parcelize.Parcelize
import org.threeten.bp.LocalDateTime

@Entity(
    tableName = "anime",
    indices = [Index(value = ["animeId"], unique = true)]
)
@Parcelize
data class AnimeEntity(
    var animeId: Int? = null,
    var malId: Int? = null,
    var simklId: Int? = null,
    @PrimaryKey
    var animeSlugId: Int,
    var name: String? = null,
    var enTitle: String? = name,
    var animeslug: String? = null,
    var description: String? = null,
    var status: String? = null,
    var image: String? = null,
    var startdate: LocalDateTime? = null,
    var enddate: LocalDateTime? = null,
    var broadcastDay: String? = null,
    var broadcastTime: String? = null,
    var source: String? = null,
    var duration: String? = null,
    var site: String? = null,
    var infoLink: String? = null,
    var createddate: String? = null,
    var type: String? = null,
    var rating: Boolean? = null,
    var favourite: Boolean = false
) : Parcelable {
    fun getImageUrl(): String {
        return "${KICKASSANIME_URL}uploads/$image"
    }

    //    since enTitle can be blank or null, we will return name if enTitle is null
    fun getDisplayTitle(): String? {
        return if (enTitle.isNullOrEmpty()) name else enTitle
    }
}
