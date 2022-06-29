// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {

    MaterialTheme {
        val scaffoldState = rememberScaffoldState()
        Scaffold(scaffoldState = scaffoldState) {
            Column(modifier = Modifier.fillMaxSize().padding(top = 30.dp)) {
                var stackTrace by remember { mutableStateOf("") }
                OutlinedTextField(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    value = stackTrace,
                    maxLines = 6,
                    onValueChange = {
                        stackTrace = it
                    })

                var cleanedUpStackTrace by remember { mutableStateOf("") }
                Button(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp), onClick = {
                    var centerRemoveText = "E AndroidRuntime:"
                    cleanedUpStackTrace = stackTrace.lines().joinToString(separator = "\n") {
                        val index = it.indexOf(centerRemoveText)
                        if (index != -1) {
                            it.substring(index + centerRemoveText.length, it.length)
                        } else {
                            it
                        }
                    }

                    centerRemoveText = "W System.err:"
                    cleanedUpStackTrace = stackTrace.lines().joinToString(separator = "\n") {
                        val index = it.indexOf(centerRemoveText)
                        if (index != -1) {
                            it.substring(index + centerRemoveText.length, it.length)
                        } else {
                            it
                        }
                    }
                }) {
                    Text("Cleanup")
                }

                if (cleanedUpStackTrace.isNotBlank()) {
                    val scroll = rememberScrollState(0)
                    Row {
                        SelectionContainer(modifier = Modifier.padding(16.dp).verticalScroll(scroll)) {
                            Text(text = cleanedUpStackTrace)
                        }
                        VerticalScrollbar(
                            modifier = Modifier.fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(scroll)
                        )

                        val clipboardManager = LocalClipboardManager.current
                        val scope = rememberCoroutineScope()
                        Button(modifier = Modifier.align(Alignment.Bottom), onClick = {
                            clipboardManager.setText(AnnotatedString(cleanedUpStackTrace))
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    message = "Copied to clipboard.",
                                    duration = SnackbarDuration.Short,
                                    actionLabel = "Ok"
                                )
                            }
                        }) {
                            Text("Copy")
                        }
                    }
                }
            }
        }
    }
}

fun main() = application {
    Window(title = "Stack trace cleaner", onCloseRequest = ::exitApplication) {
        App()
    }
}
