package com.plcoding.cleanarchitecturenoteapp.feature_note.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.plcoding.cleanarchitecturenoteapp.ui.*

// color存成Color类型，数据库会出错！！
@Entity
data class Note(
    val title: String,
    val content: String,
    val timeStamp: Long,

    // 这里数据库没有保存toggle状态，所以不会回复颜色值的toggle状态
    // 同理，数据库没有保存最后一次用户自定义的颜色，所以也不会回复
    val color: Int,
    val url: String?,

    @PrimaryKey val id: Int? = null
) {
    // 常伴的五个圈，六个圈的取值
    companion object {
        var noteColors = listOf(RedOrange, LightGreen, Violet, BabyBlue, RedPink)
        // val cusColor: Int? = -1 // 这样是不可以的
    }
}

class InvalidNoteException(message: String): Exception(message)