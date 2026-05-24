package com.example.dowload_rouz

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import androidx.tv.material3.MaterialTheme
import coil.compose.AsyncImage
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

// --- FONTS ---
val Font0 = FontFamily(Font(R.font.cascadia_mono))
val Font1 = FontFamily(Font(R.font.noto_regular))
val Font2 = FontFamily(Font(R.font.lalezar_regular))

@OptIn(ExperimentalTvMaterial3Api::class)
val TvCustomTypography = Typography(
    displayLarge = androidx.tv.material3.Typography().displayLarge.copy(fontFamily = Font1),
    displayMedium = androidx.tv.material3.Typography().displayMedium.copy(fontFamily = Font1),
    displaySmall = androidx.tv.material3.Typography().displaySmall.copy(fontFamily = Font1),
    headlineLarge = androidx.tv.material3.Typography().headlineLarge.copy(fontFamily = Font1),
    headlineMedium = androidx.tv.material3.Typography().headlineMedium.copy(fontFamily = Font1),
    headlineSmall = androidx.tv.material3.Typography().headlineSmall.copy(fontFamily = Font1),
    titleLarge = androidx.tv.material3.Typography().titleLarge.copy(fontFamily = Font1),
    titleMedium = androidx.tv.material3.Typography().titleMedium.copy(fontFamily = Font1),
    titleSmall = androidx.tv.material3.Typography().titleSmall.copy(fontFamily = Font1),
    labelLarge = androidx.tv.material3.Typography().labelLarge.copy(fontFamily = Font1),
    labelMedium = androidx.tv.material3.Typography().labelMedium.copy(fontFamily = Font1),
    labelSmall = androidx.tv.material3.Typography().labelSmall.copy(fontFamily = Font1),
    bodyLarge = androidx.tv.material3.Typography().bodyLarge.copy(fontFamily = Font1),
    bodyMedium = androidx.tv.material3.Typography().bodyMedium.copy(fontFamily = Font1),
    bodySmall = androidx.tv.material3.Typography().bodySmall.copy(fontFamily = Font1)
)

// --- MAIN ACTIVITY ---
@OptIn(ExperimentalTvMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this) {}

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme(typography = TvCustomTypography) {

                    // 1. Create the Navigator
                    val navController = rememberNavController()

                    // 2. Set up the routes.
                    // Start at "home" temporarily to bypass the IP ban!
                    NavHost(navController = navController, startDestination = "home") {

                        composable("login") {
                            ModernTvLoginScreen()
                        }

                        composable("home") {
                            TvHomeScreen()
                        }
                    }

                }
            }
        }
    }
}

// --- LOGIN COMPONENTS ---
@Composable
fun AdMobBanner() {
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun LoginWebView(baseUrl: String, user: String, pass: String, phone: String, onBackPressed: () -> Unit) {
    BackHandler(onBack = onBackPressed)

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)

                        val checkErrorJs = """
                            (function() {
                                if (document.body.innerText.includes("Your IP has been blocked")) {
                                    return "BLOCKED";
                                }
                                return "OK";
                            })();
                        """.trimIndent()

                        view.evaluateJavascript(checkErrorJs) { result ->
                            if (result == "\"BLOCKED\"") {
                                Toast.makeText(context, "خطا: آی‌پی شما مسدود شده است", Toast.LENGTH_LONG).show()
                                onBackPressed()
                            } else {
                                val injectJs = """
                                    setTimeout(function() {
                                        var userField = document.querySelector('input[name="username"]');
                                        var passField = document.querySelector('input[name="password"]');
                                        var submitBtn = document.querySelector('button[type="submit"]');
                                        
                                        if(userField && passField) {
                                            userField.value = '$user';
                                            passField.value = '$pass';
                                            if(submitBtn) {
                                                setTimeout(function() { submitBtn.click(); }, 300);
                                            }
                                        } 
                                        else {
                                            var phoneField = document.querySelector('input[name="phone"], input[name="mobile"], input[type="tel"]');
                                            if(phoneField) {
                                                phoneField.value = '$phone';
                                                if(submitBtn) {
                                                    setTimeout(function() { submitBtn.click(); }, 300);
                                                }
                                            }
                                        }
                                    }, 500); 
                                """.trimIndent()

                                view.evaluateJavascript(injectJs, null)

                                val focusCss = """
                                    var style = document.createElement('style');
                                    style.innerHTML = `*:focus { outline: 5px solid #FFC107 !important; transform: scale(1.05) !important; z-index: 9999 !important; }`;
                                    document.head.appendChild(style);
                                """.trimIndent()
                                view.evaluateJavascript(focusCss, null)
                            }
                        }
                    }
                }

                val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                loadUrl("${formattedUrl}user/login/")
            }
        }
    )
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun ModernTvLoginScreen() {
    var serverUrl by remember { mutableStateOf("https://jenna.steve-mcqueen.com/") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var showWebView by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFF121212)
    val panelColor = Color(0xFF1E1E1E)
    val primaryColor = Color(0xFF2F4D6C)
    val textColor = Color.White
    val mutedTextColor = Color(0xFFAAAAAA)

    if (showWebView) {
        LoginWebView(
            baseUrl = serverUrl,
            user = username,
            pass = password,
            phone = phoneNumber,
            onBackPressed = { showWebView = false }
        )
    } else {
        Row(modifier = Modifier.fillMaxSize().background(backgroundColor)) {

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().background(panelColor).padding(horizontal = 48.dp, vertical = 36.dp),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = stringResource(id = R.string.welcome_back),
                    color = textColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Font2,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = stringResource(id = R.string.verify_credentials),
                    color = mutedTextColor,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text(stringResource(id = R.string.server_address), color = mutedTextColor) },
                    singleLine = true,
                    textStyle = TextStyle(textDirection = TextDirection.Ltr, fontFamily = Font0),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor, unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = textColor, unfocusedTextColor = textColor
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(id = R.string.username), color = mutedTextColor) },
                    singleLine = true,
                    textStyle = TextStyle(textDirection = TextDirection.Ltr, fontFamily = Font0),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor, unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = textColor, unfocusedTextColor = textColor
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(id = R.string.password), color = mutedTextColor) },
                    singleLine = true,
                    textStyle = TextStyle(textDirection = TextDirection.Ltr, fontFamily = Font0),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor, unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = textColor, unfocusedTextColor = textColor
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number", color = mutedTextColor) },
                    singleLine = true,
                    textStyle = TextStyle(textDirection = TextDirection.Ltr, fontFamily = Font0),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor, unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = textColor, unfocusedTextColor = textColor
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                )

                Button(
                    onClick = { showWebView = true },
                    colors = ButtonDefaults.colors(containerColor = primaryColor, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        androidx.tv.material3.Text(text = stringResource(id = R.string.proceed_to_login), fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                AdMobBanner()
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = stringResource(id = R.string.app_credits), color = Color.DarkGray, fontSize = 12.sp)
            }

            Box(modifier = Modifier.weight(1.2f).fillMaxHeight().padding(32.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.test_poster),
                    contentDescription = stringResource(id = R.string.image_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                )
            }
        }
    }
}

// --- HOME SCREEN COMPONENTS ---

data class MediaItem(val id: String, val title: String, val posterUrl: String, val extraInfo: String = "")

val featuredFilms = listOf(
    MediaItem("1", "Project Hail Mary", "https://steve-mcqueen.com/img/312-468/8a43981d46f055dd02055e6846331e42.jpg.webp"),
    MediaItem("2", "Remarkably Bright Creatures", "https://steve-mcqueen.com/img/312-468/7171693587e0366f1305e4b8116f4a8f.jpg.webp"),
    MediaItem("3", "Swapped", "https://steve-mcqueen.com/img/312-468/e1f89682041d5cb4b37186a4096ce216.jpg.webp"),
    MediaItem("4", "Crime 101", "https://steve-mcqueen.com/img/312-468/aaaa65750096f93424fd7e19aa207246.jpg.webp"),
    MediaItem("5", "The President's Cake", "https://steve-mcqueen.com/img/312-468/200ea798de217ce4c2dbdaf2d2d93634.jpg.webp"),
    MediaItem("6", "Avatar: Fire and Ash", "https://steve-mcqueen.com/img/312-468/07c79ea273b47363da9d512430d8c0a9.jpg.webp")
)

val recentStreams = listOf(
    MediaItem("7", "Jack Ryan: Ghost War", "https://steve-mcqueen.com/img/354-532/e99d5c1dfd45e064e0fa942d982c45ad.jpg.webp"),
    MediaItem("8", "A Brother and 7 Siblings", "https://steve-mcqueen.com/img/354-532/104e4a78dcfc031935e7d91040de7d9f.jpg.webp"),
    MediaItem("9", "Jew", "https://steve-mcqueen.com/img/354-532/64063827bef11aa2afa11202f576cead.jpg.webp"),
    MediaItem("10", "Mother Mary", "https://steve-mcqueen.com/img/354-532/ddb77f99489d17d6852adf438c128455.jpg.webp")
)

val updatedSeries = listOf(
    MediaItem("11", "The Boys", "https://steve-mcqueen.com/img/170-256/dfe048fbd581c9c3549582ceadb35e4f.jpg.webp", "قسمت ۰۸ فصل پنجم"),
    MediaItem("12", "Off Campus", "https://steve-mcqueen.com/img/170-256/e643e176cda9c9bf3b11899d6d7443e7.jpg.webp", "قسمت ۰۸ فصل اول"),
    MediaItem("13", "Dutton Ranch", "https://steve-mcqueen.com/img/170-256/c4de75643333bd43d6993feef92f20c4.jpg.webp", "قسمت ۰۳"),
    MediaItem("14", "Euphoria", "https://steve-mcqueen.com/img/170-256/de7048418c36bdafb511cd64b683b95f.jpg.webp", "قسمت ۰۶ فصل سوم")
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvHomeScreen() {
    val backgroundColor = Color(0xFF121212)

    TvLazyColumn(
        modifier = Modifier.fillMaxSize().background(backgroundColor),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item { HeroBanner() }
        item { MediaCategoryRow(title = "فیلم های برگزیده", items = featuredFilms) }
        item { MediaCategoryRow(title = "مشاهدات اخیر پخش آنلاین", items = recentStreams) }
        item { MediaCategoryRow(title = "سریال های بروز شده", items = updatedSeries) }
    }
}

@Composable
fun HeroBanner() {
    Box(
        modifier = Modifier.fillMaxWidth().height(400.dp).padding(bottom = 24.dp)
    ) {
        AsyncImage(
            model = "https://steve-mcqueen.com/img/1504-846/3da75cae36c9ea29205b489d2ca92f33.jpg.webp",
            contentDescription = "Hero Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFF121212)),
                    startY = 100f
                )
            )
        )
        Text(
            text = "Project Hail Mary",
            color = Color.White,
            fontSize = 42.sp,
            fontFamily = Font2,
            modifier = Modifier.align(Alignment.BottomStart).padding(32.dp)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaCategoryRow(title: String, items: List<MediaItem>) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Font2,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )

        TvLazyRow(
            contentPadding = PaddingValues(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explicitly use the items extension function
            androidx.tv.foundation.lazy.list.items(items) { item ->
                Card(
                    onClick = { /* Handle clicking the movie */ },
                    modifier = Modifier.width(150.dp).aspectRatio(2f / 3f)
                ) {
                    AsyncImage(
                        model = item.posterUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}