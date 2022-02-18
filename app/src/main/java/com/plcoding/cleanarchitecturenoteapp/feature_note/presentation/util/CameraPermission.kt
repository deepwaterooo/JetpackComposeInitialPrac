package com.plcoding.cleanarchitecturenoteapp.feature_note.presentation.util

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState

//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun Permission(
//    permission: String = android.Manifest.permission.CAMERA,
//    rationale: String = "This permission is important for this app. Please grant the permission.",
//    permissionNotAvailableContent: @Composable () -> Unit = { },
//    content: @Composable () -> Unit = { }
//) {
//    val permissionState = rememberPermissionState(permission)
//    PermissionRequired( //
//        permissionState = permissionState,
//        permissionNotGrantedContent = {
//            Rationale(
//                text = rationale,
//                onRequestPermission = { permissionState.launchPermissionRequest() }
//            )
//        },
//        permissionNotAvailableContent = permissionNotAvailableContent,
//        content = content
//    )
//}
//
//// 这里是自己加的
//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun PermissionRequired(
//    permissionState: PermissionState,
//    permissionNotGrantedContent: () -> Unit,
//    permissionNotAvailableContent: @Composable () -> Unit,
//    content: @Composable () -> Unit
//) {
////    TODO("Not yet implemented")
//}
//
//@Composable
//private fun Rationale(
//    text: String,
//    onRequestPermission: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = { /* Don't */ },
//        title = {
//            Text(text = "Permission request")
//        },
//        text = {
//            Text(text)
//        },
//        confirmButton = {
//            Button(onClick = onRequestPermission) {
//                Text("Ok")
//            }
//        }
//    )
//}