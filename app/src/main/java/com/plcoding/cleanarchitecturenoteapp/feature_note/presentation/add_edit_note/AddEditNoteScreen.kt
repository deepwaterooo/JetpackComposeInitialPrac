package com.plcoding.cleanarchitecturenoteapp.feature_note.presentation.add_edit_note

import android.net.Uri
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.plcoding.cleanarchitecturenoteapp.R
import com.plcoding.cleanarchitecturenoteapp.feature_note.domain.model.Note
import com.plcoding.cleanarchitecturenoteapp.feature_note.presentation.add_edit_note.components.*
import com.plcoding.cleanarchitecturenoteapp.feature_note.presentation.util.DEFAULT_RECIPE_IMAGE
import com.plcoding.cleanarchitecturenoteapp.feature_note.presentation.util.GallerySelect
import com.plcoding.cleanarchitecturenoteapp.feature_note.presentation.util.loadPicture
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// 这里面关于相机的部分的状态没有理清楚，所以导致了图片选择库这个地方有个blocking bug，have to fix before moving on
@OptIn(ExperimentalAnimationApi::class,
    com.google.accompanist.permissions.ExperimentalPermissionsApi::class
)
@Composable // 每条便签一编写界面
fun AddEditNoteScreen (
    navController: NavController,
    noteColorState: NoteColorState,
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    val TAG = "test AddEditNoteScreen"

    val titleState = viewModel.noteTitle.value
    val contentState = viewModel.noteContent.value
    val colorState = viewModel.noteColor.value
    val colorCusState = viewModel.noteCusColor.value
    val imageState = viewModel.noteImage.value

    val imageUri = viewModel.imgUri
    var showGallery = viewModel.showGallery

    // var showImage = viewModel.showImage.value

    val scaffoldState = rememberScaffoldState()

    val noteBackgroundAnimatable = remember {
        Animatable(
            Color(if (noteColorState.color != -1) noteColorState.color else viewModel.noteColor.value.color)
        )
    }

    val scope = rememberCoroutineScope()

    // LaunchEffect允许我们在Composable中使用协程
    // 让Composable支持协程的重要意义是，可以让一些简单的业务逻辑直接以Composable的形式封装并实现复用，而无需额外借助ViewModel
    // key1 = true so that execute only once
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditNoteViewModel.UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }
                is AddEditNoteViewModel.UiEvent.SaveNote -> {
                    navController.navigateUp()
                }
            }
        }
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(AddEditNoteEvent.SaveNote)
                    // navController.navigate(AddEditNoteScreen.route) // 拿来参考的,没用上
                },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Save note")
            }
        },
        scaffoldState = scaffoldState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(noteBackgroundAnimatable.value)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Note.noteColors.forEach { color ->
                    val colorInt = color.toArgb()
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(10.dp, CircleShape)
                            .background(color)
                            // 这里把边框去掉了，只是埋掉了一个bug，因为我有新的兴趣在，暂时不处理这个bug,改天回来再改
                            // .border(width = 3.dp,
                            //     // 这里是画圆圈边圈的颜色:感觉更新得不对，总是上一次的颜色在画圈
                            //     color = if (viewModel.noteColor.value.color == colorInt) {
                            //         Color.Black
                            //     } else 
                            //         Color.Transparent
                            //     },
                            //     shape = CircleShape
                            // )
                            .clickable {
                                scope.launch {
                                    noteBackgroundAnimatable.animateTo(
                                        targetValue = Color(colorInt),
                                        animationSpec = tween(
                                            durationMillis = 500
                                        )
                                    )
                                    viewModel.onEvent(AddEditNoteEvent.ChangeColor(color))
                                }
                            }
                    )
                }
                IconButton(
                    onClick = { // BUG #2: 自定义颜色：这里不知道是哪里的原因，残留了一个背景圆圈的背景，需要fix掉
                        viewModel.onEvent(AddEditNoteEvent.ToggleColorSection)
                    },
                ) {
                    // Box (modifier = Modifier
                    //         .size(50.dp)
                    //         .shadow(15.dp, CircleShape)
                    //         .background(
                    //             // 这里把它设定为了colorState值更新前的value，即上一次选定的颜色，所以会受其它按钮的影响，可以用一个值把它记住，就不会悥记了
                    //             // Color(if (Note.cusColor != -1) Note.cusColor else viewModel.noteColor.value.color), // 这里仍然不对
                    //             Color(colorState.color), 
                    //             shape = CircleShape // 想把这里改成心形
                    //         )
                    //     // // bug #1: 如果我定义了圆圈的黑圈描边，那么会仍然需要控制变量、当点击了其它背景颜色时来取消现自定义黑圈边，不如暂且简单不管它
                    //     //     .border(
                    //     //         width = 3.dp,
                    //     //         // 这里是画圆圈边圈的颜色
                    //     //         color = if (Color(colorState.color) != Color.Black) {
                    //     //             Color.Black
                    //     //         } else {
                    //     //             Color.Transparent
                    //     //         },
                    //     //         shape = CircleShape
                    //     //     )
                    // )
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favorite",
                        modifier = Modifier.size(150.dp),
                        tint = (if (colorCusState == -1) Color.Red else Color(colorCusState))
                    )
                }
                IconButton(
                    // 再加一个 选择图片 imagebutton, 再加一个相机
                    onClick = {
                        Log.d(TAG, "onClick toggleImageSection")
                        // viewModel.onEvent(AddEditNoteEvent.ToggleGallerySection)
                        viewModel.onEvent(AddEditNoteEvent.ToggleImageSection)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = "Images",
                        modifier = Modifier.size(150.dp)
                    )
                    Text("Img")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 选择自定义颜色栏 是 可见 可不见的，根据用户的点击状态来决定是否可见
            AnimatedVisibility(
                visible = colorState.isColorSectionVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                PickAColorSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    noteColor = Color(colorState.color),
                    onColorChange = {
                        viewModel.onEvent(AddEditNoteEvent.ChangeColor(it))
                        scope.launch {
                            noteBackgroundAnimatable.animateTo(
                                targetValue = it,
                                animationSpec = tween(
                                    durationMillis = 500
                                )
                            )
                            viewModel.onEvent(AddEditNoteEvent.ChangeCusColor(it))
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TransparentHintTextField(
                text = titleState.text,
                hint = titleState.hint,
                onValueChange = {
                    viewModel.onEvent((AddEditNoteEvent.EnteredTitle(it)))
                },
                onFocusChange = {
                    viewModel.onEvent(AddEditNoteEvent.ChangeTitleFocus(it))
                },
                isHintVisible = titleState.isHintVisible,
                singleLine = true,
                textStyle = MaterialTheme.typography.h5,
                modifier = Modifier
            )
            Spacer(modifier = Modifier.height(16.dp))
            TransparentHintTextField(
                text = contentState.text,
                hint = contentState.hint,
                onValueChange = {
                    viewModel.onEvent((AddEditNoteEvent.EnteredContent(it)))
                },
                onFocusChange = {
                    viewModel.onEvent(AddEditNoteEvent.ChangeContentFocus(it))
                },
                isHintVisible = contentState.isHintVisible,
                textStyle = MaterialTheme.typography.body1,
                // modifier = Modifier.fillMaxHeight() // 因为这里已经占据了整个屏幕，你是看不见后面再加的图片的
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // // 加载图片栏： 是 可见 可不见的，根据用户的点击状态来决定是否可
            Log.d(TAG, "imageState.isImageSectionVisible: " + imageState.isImageSectionVisible)
            AnimatedVisibility(
                // visible = showGallery.value,// 不 work
                visible = imageState.isImageSectionVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                ImageMainContent(
                    // modifier: Modifier = Modifier,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(top = 8.dp) // adding some space to the label
                        .background(Color(colorState.color)),
                    // imageUri,
                    // showGallery,
//                    showGallerySelect
                    viewModel
                )
                // ImageMainContent(
                //     modifier = Modifier
                //         .fillMaxWidth()
                //         .fillMaxHeight()
                //         .padding(top = 8.dp) // adding some space to the label
                //         .background(Color(colorState.color))
                // )
            }
        }
    }
}