package com.otaku.kickassanime.db.models.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.otaku.kickassanime.Strings.KICKASSANIME_URL
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "anime"
)
@Parcelize
data class AnimeEntity(
    @PrimaryKey
    var animeSlug: String,
    var name: String? = null,
    var description: String? = null,
    var image: String? = null,
    var status: String? = null,
    var type: String? = null,
    var rating: String? = null,
    var favourite: Boolean = false,
    val year: Int? = null
) : Parcelable {
    fun getImageUrl(): String {
        return "${KICKASSANIME_URL}image/poster/$image"
    }
}

class AnimeEntityWithPage(
    @Embedded
    val animeEntity: AnimeEntity,
    val pageNumber: Int
) {
    override fun equals(other: Any?): Boolean {
        return if (other is AnimeEntityWithPage) {
            other.pageNumber == pageNumber && other.animeEntity.animeSlug == this.animeEntity.animeSlug
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = animeEntity.animeSlug.hashCode()
        result = 31 * result + pageNumber
        return result
    }
}