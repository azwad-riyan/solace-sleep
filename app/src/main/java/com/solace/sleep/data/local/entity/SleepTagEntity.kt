package com.solace.sleep.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.solace.sleep.domain.model.SleepTag

@Entity(
    tableName = "sleep_tags",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profile_id"])]
)
data class SleepTagEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "profile_id")
    val profileId: String,

    @ColumnInfo(name = "label")
    val label: String,

    @ColumnInfo(name = "emoji")
    val emoji: String,

    @ColumnInfo(name = "is_default")
    val isDefault: Boolean
) {
    fun toDomain() = SleepTag(
        id = id,
        profileId = profileId,
        label = label,
        emoji = emoji,
        isDefault = isDefault
    )

    companion object {
        fun fromDomain(tag: SleepTag) = SleepTagEntity(
            id = tag.id,
            profileId = tag.profileId,
            label = tag.label,
            emoji = tag.emoji,
            isDefault = tag.isDefault
        )
    }
}
