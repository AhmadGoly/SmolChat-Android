/*
 * Copyright (C) 2024 Shubham Panchal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shubham0204.smollmandroid.ui.screens.model_download

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.shubham0204.smollmandroid.llm.exampleModelsList
import io.shubham0204.smollmandroid.ui.components.AppProgressDialog
import io.shubham0204.smollmandroid.ui.screens.chat.ChatActivity
import io.shubham0204.smollmandroid.ui.theme.AppAccentColor
import io.shubham0204.smollmandroid.ui.theme.AppFontFamily
import io.shubham0204.smollmandroid.ui.theme.SmolLMAndroidTheme
import org.koin.androidx.compose.koinViewModel

class DownloadModelActivity : ComponentActivity() {
    private var openChatScreen: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DownloadModelScreen() }

        openChatScreen = intent.extras?.getBoolean("openChatScreen") ?: true
    }

    private fun openChatActivity() {
        if (openChatScreen) {
            Intent(this, ChatActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        } else {
            finish()
        }
    }

    @Composable
    private fun DownloadModelScreen() {
        val viewModel: DownloadModelsViewModel = koinViewModel()

        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                activityResult.data?.let {
                    it.data?.let { uri ->
                        viewModel.copyModelFile(uri, onComplete = { openChatActivity() })
                    }
                }
            }
        SmolLMAndroidTheme {
            Column(
                modifier =
                    Modifier
                        .background(Color.White)
                        .fillMaxSize()
                        .padding(16.dp)
                        .windowInsetsPadding(WindowInsets.safeContent)
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    modifier = Modifier.size(100.dp),
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = AppAccentColor,
                )
                Text(
                    "Download Models",
                    fontFamily = AppFontFamily,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Popular Models",
                    fontFamily = AppFontFamily,
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(4.dp))
                ModelsList(viewModel)
                Spacer(modifier = Modifier.height(4.dp))
                ModelURLInput(viewModel)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    enabled =
                        viewModel.selectedModelState.value != null ||
                            viewModel.modelUrlState.value.isNotBlank(),
                    onClick = { viewModel.downloadModel() },
                ) {
                    Text("Download Model", fontFamily = AppFontFamily)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Models are downloaded in the 'Downloads' directory of your device",
                    fontFamily = AppFontFamily,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Select a GGUF file from the file explorer",
                    fontFamily = AppFontFamily,
                    style = MaterialTheme.typography.titleSmall,
                )
                OutlinedButton(
                    onClick = {
                        val intent =
                            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                setType("application/octet-stream")
                                putExtra(
                                    DocumentsContract.EXTRA_INITIAL_URI,
                                    Environment
                                        .getExternalStoragePublicDirectory(
                                            Environment.DIRECTORY_DOWNLOADS,
                                        ).toUri(),
                                )
                            }
                        launcher.launch(intent)
                    },
                ) {
                    Text("Select GGUF file", fontFamily = AppFontFamily)
                }
            }
            AppProgressDialog()
        }
    }

    @Composable
    private fun ModelsList(viewModel: DownloadModelsViewModel) {
        var selectedModel by remember { viewModel.selectedModelState }
        Column(verticalArrangement = Arrangement.Center) {
            exampleModelsList.forEach { model ->
                Row(
                    Modifier
                        .clickable { selectedModel = model }
                        .fillMaxWidth()
                        .background(
                            if (model == selectedModel) AppAccentColor else Color.White,
                            RoundedCornerShape(
                                8.dp,
                            ),
                        ).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (model == selectedModel) {
                        Icon(Icons.Default.Done, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        color = if (model == selectedModel) Color.White else Color.Black,
                        text = model.name,
                        fontFamily = AppFontFamily,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }

    @Composable
    private fun ModelURLInput(viewModel: DownloadModelsViewModel) {
        var modelUrl by remember { viewModel.modelUrlState }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = modelUrl,
            onValueChange = { modelUrl = it },
            placeholder = {
                Text(text = "URL for GGUF Instruct Model", fontFamily = AppFontFamily)
            },
            textStyle = TextStyle(fontFamily = AppFontFamily),
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri,
                ),
        )
    }
}
