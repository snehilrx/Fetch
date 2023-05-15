package com.otaku.fetch

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otaku.fetch.base.R
import com.otaku.fetch.base.utils.UiUtils.statusBarHeight
import com.otaku.kickassanime.ui.theme.KickassAnimeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val sohen = FontFamily(
        Font(R.font.sohne_fett, FontWeight.Bold)
    )
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KickassAnimeTheme {
                Scaffold { contentPadding ->
                    // Screen content
                    Box(modifier = Modifier.padding(contentPadding)) {
                        Column(
                            modifier = Modifier.padding(16.dp, 24.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.Center) {
                                Text(
                                    text = "Fetch!",
                                    fontFamily = sohen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 42.sp
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                ModuleGrid()
                            }
                        }
                    }
                }
            }
        }
    }

    @Preview(showBackground = true, widthDp = 320, heightDp = 320)
    @Composable
    private fun ModuleGrid() {
        val modules = ModuleRegistry.getModulesList().toMutableList()
        LazyColumn(
            verticalArrangement = Arrangement.Center
        ) {
            items(modules.size,
                key = { modules[it].displayName }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues = PaddingValues(32.dp, 0.dp))
                        .background(Color.Transparent)
                        .clickable {
                            launchModule(modules[it])
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        modules[it].displayIcon?.let { icon ->
                            Image(
                                painter = painterResource(id = icon),
                                contentDescription = modules[it].displayName,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun launchModule(data: ModuleRegistry.ModuleData) {
        statusBarHeight
        (application as? FetchApplication)?.currentModule = data.appModule
        val moduleIntent = Intent(this, ModuleActivity::class.java)
        moduleIntent.putExtra(ModuleActivity.ARG_MODULE_NAME, data.displayName)
        startActivity(moduleIntent)
        finish()
    }


}