package com.plcoding.cleanarchitecturenoteapp.feature_note.presentation.add_edit_note.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.cleanarchitecturenoteapp.feature_note.domain.model.InvalidNoteException
import com.plcoding.cleanarchitecturenoteapp.feature_note.domain.model.Note
import com.plcoding.cleanarchitecturenoteapp.feature_note.domain.use_case.NoteUseCases
import com.plcoding.cleanarchitecturenoteapp.feature_note.presentation.add_edit_note.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

val EMPTY_IMAGE_URI: Uri = Uri.parse("file://dev/null")
// val url = "https://nyc3.digitaloceanspaces.com/food2fork/food2fork-static/featured_images/500/featured_image.png"
val url = ""

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val TAG = "test AddEditNoteViewModel"

    private val _noteTitle = mutableStateOf(NoteTextFieldState(
                                                hint = "Enter title..."
    ))
    val noteTitle: State<NoteTextFieldState> = _noteTitle

    private val _noteContent = mutableStateOf(NoteTextFieldState(
                                                  hint = "Enter some content..."
    ))
    val noteContent: State<NoteTextFieldState> = _noteContent

    private val _noteColor = mutableStateOf<NoteColorState>(NoteColorState(Note.noteColors.random().toArgb(), false))
    val noteColor: State<NoteColorState> = _noteColor
    private val _noteCusColor = mutableStateOf<Int>(-1)
    val noteCusColor: State<Int> = _noteCusColor

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentNoteId: Int? = null
    
    private val _noteImage = mutableStateOf(NoteImageState(EMPTY_IMAGE_URI, false, url))
    val noteImage: State<NoteImageState> = _noteImage
    
    private var _imgUri = mutableStateOf(EMPTY_IMAGE_URI)
    val imgUri: MutableState<Uri> = _imgUri

    private val _showGallery = mutableStateOf<Boolean>(false)
    val showGallery: MutableState<Boolean> = _showGallery

    init {
        savedStateHandle.get<Int>("noteId")?.let {
            noteId ->
                if (noteId != -1) {
                    viewModelScope.launch {
                        noteUseCases.getNote(noteId)?.also {
                            note ->
                                currentNoteId = note.id
                            _noteTitle.value = noteTitle.value.copy(
                                text = note.title,
                                isHintVisible = false
                            )
                            _noteContent.value = noteContent.value.copy(
                                text = note.content,
                                isHintVisible = false
                            )
                            _noteColor.value = noteColor.value.copy(
                                color = note.color,
                                isColorSectionVisible = note.isColorSectionVisible
                            )
                            _noteCusColor.value = note.cusColor
                            if (note.url != null || note.uri != "") {
                                _noteImage.value.uri = Uri.parse(note.uri)
                                imgUri.value = Uri.parse(note.uri)
                                _noteImage.value.isImageSectionVisible = note.isImageSectionVisible
                                _noteImage.value.url = note.url
                            }
                        }
                    }
                }
        }
    }
    fun onEvent(event: AddEditNoteEvent) {
        when(event) {
            is AddEditNoteEvent.SaveNote -> {
                viewModelScope.launch {
                    try {
                        noteUseCases.addNote(
                            Note(
                                title = noteTitle.value.text,
                                content = noteContent.value.text,
                                timeStamp = System.currentTimeMillis(),
                                color = noteColor.value.color,
                                isColorSectionVisible = noteColor.value.isColorSectionVisible,
                                cusColor = noteCusColor.value,
                                // uri = noteImage.value.uri.toString(), // 这里保存的是uri string，但加载的时候
                                uri = imgUri.value.toString(),
                                isImageSectionVisible = noteImage.value.isImageSectionVisible,
                                url = noteImage.value.url, // 这里保存的是url string，但加载的时候
                                id = currentNoteId
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveNote)
                    } catch (e: InvalidNoteException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Coundn't save note"
                            )
                        )
                    }
                }
            }

            // 选择一个favoriate颜色: 这里要理解一下event的事件传递方向
            is AddEditNoteEvent.ToggleColorSection -> {
                _noteColor.value = noteColor.value.copy(
                    isColorSectionVisible = !noteColor.value.isColorSectionVisible
                )
            }
            is AddEditNoteEvent.ChangeColor -> {
                _noteColor.value.color = event.color.toArgb()
            }
            is AddEditNoteEvent.ChangeCusColor -> {
                _noteCusColor.value = event.color.toArgb()
            }

            is AddEditNoteEvent.ToggleImageSection -> {
                Log.d(TAG, "ToggleImageSection")
                _showGallery.value = !showGallery.value
                _noteImage.value = noteImage.value.copy(
                    isImageSectionVisible = !noteImage.value.isImageSectionVisible
                )
            }
            is AddEditNoteEvent.LoadImageUri -> {
                Log.d(TAG, "event.uri.toString(): " + event.toString())
                _imgUri = imgUri
                _noteImage.value.uri = imgUri.value
            }
//            is AddEditNoteEvent.LoadImageUrl -> {
//                _noteImage.value.url = noteImage.value.url
//            }
            is AddEditNoteEvent.RemoveImage -> {
                _noteImage.value.uri = noteImage.value.uri
                _noteImage.value.url = noteImage.value.url
                _imgUri.value = imgUri.value
            }
            
            is AddEditNoteEvent.EnteredTitle -> {
                _noteTitle.value = noteTitle.value.copy(
                    text = event.value
                )
            }     
            is AddEditNoteEvent.ChangeTitleFocus -> {
                _noteTitle.value = noteTitle.value.copy(
                    isHintVisible = !event.focusState.isFocused
                    && _noteTitle.value.text.isBlank()
                )
            }     
            is AddEditNoteEvent.EnteredContent -> {
                _noteContent.value = _noteContent.value.copy(
                    text = event.value
                )
            }     
            is AddEditNoteEvent.ChangeContentFocus -> {
                _noteContent.value = _noteContent.value.copy(
                    isHintVisible = !event.focusState.isFocused
                    && _noteContent.value.text.isBlank()
                )
            }     
        }
    }
    sealed class UiEvent {
        data class ShowSnackbar(val message: String): UiEvent()
        object SaveNote: UiEvent()

        object PickAColor: UiEvent()
    }
}
