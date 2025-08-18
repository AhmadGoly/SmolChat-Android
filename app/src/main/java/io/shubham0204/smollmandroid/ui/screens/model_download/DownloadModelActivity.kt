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
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.File
import io.shubham0204.smollmandroid.R
import io.shubham0204.smollmandroid.ui.components.AppAlertDialog
import io.shubham0204.smollmandroid.ui.components.AppBarTitleText
import io.shubham0204.smollmandroid.ui.components.AppProgressDialog
import io.shubham0204.smollmandroid.ui.components.createAlertDialog
import io.shubham0204.smollmandroid.ui.screens.chat.ChatActivity
import io.shubham0204.smollmandroid.ui.theme.SmolLMAndroidTheme
import org.koin.android.ext.android.inject

class DownloadModelActivity : ComponentActivity() {

    private var openChatScreen: Boolean = true
    private val viewModel: DownloadModelsViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmolLMAndroidTheme {
                DownloadModelScreen(
                    onBackClick = { finish() },
                    viewModel = viewModel,
                    onFileSelected = { uri ->
                        if (checkGGUFFile(uri)) {
                            viewModel.copyModelFile(uri, onComplete = { openChatActivity() })
                        } else {
                            createAlertDialog(
                                dialogTitle = getString(R.string.dialog_invalid_file_title),
                                dialogText = getString(R.string.dialog_invalid_file_text),
                                dialogPositiveButtonText = "OK",
                                onPositiveButtonClick = {},
                                dialogNegativeButtonText = null,
                                onNegativeButtonClick = null,
                            )
                        }
                    }
                )
            }
        }
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

    // check if the first four bytes of the file
    // represent the GGUF magic number
    // see:https://github.com/ggml-org/ggml/blob/master/docs/gguf.md#file-structure
    private fun checkGGUFFile(uri: Uri): Boolean {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val ggufMagicNumberBytes = ByteArray(4)
            inputStream.read(ggufMagicNumberBytes)
            return ggufMagicNumberBytes.contentEquals(byteArrayOf(71, 71, 85, 70))
        }
        return false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadModelScreen(
    onBackClick: () -> Unit,
    viewModel: DownloadModelsViewModel,
    onFileSelected: (Uri) -> Unit
) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            activityResult.data?.let {
                it.data?.let { uri ->
                    onFileSelected(uri)
                }
            }
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { AppBarTitleText(stringResource(R.string.add_new_model_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            FeatherIcons.ArrowLeft,
                            contentDescription = "Navigate Back",
                            tint = colorResource(id = R.color.dark_blue_text),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.light_blue_bg)
                )
            )
        },
    ) { innerPadding ->
        Surface(
            modifier =
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(colorResource(id = R.color.light_blue_bg)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "مدل های زبان بزرگ (LLM) چیست؟",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colorResource(id = R.color.dark_blue_text),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "مدل های زبان بزرگ یا LLM ها سیستم های هوش مصنوعی هستند که می توانند متن را بفهمند و تولید کنند. آنها بر روی مجموعه داده های عظیم متن و کد آموزش دیده اند. در این برنامه می توانید از مدل های مختلفی برای کارهای مختلف مانند پاسخ به سوالات، خلاصه کردن متن و موارد دیگر استفاده کنید. برای شروع، لطفاً یک فایل مدل GGUF را از دستگاه خود انتخاب کنید.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Justify
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
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
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.dark_blue_text)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        FeatherIcons.File,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.download_models_select_gguf_button),
                        color = Color.White
                    )
                }
            }
            AppProgressDialog()
            AppAlertDialog()
        }
    }
}
