package com.solace.sleep.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "session_tag_cross_ref",
    primaryKeys = ["session_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = SleepSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SleepTagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["tag_id"])
    ]
)
data class SessionTagCrossRef(
    @ColumnInfo(name = "session_id")
    val sessionId: String,

    @ColumnInfo(name = "tag_id")
    val tagId: String
)
