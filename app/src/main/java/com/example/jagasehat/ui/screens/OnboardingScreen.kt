package com.example.jagasehat.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            "Selamat Datang! \uD83D\uDC4B",
            "JagaSehat adalah asisten pintar untuk mencatat dan memantau kesehatan seluruh keluarga Anda dengan sangat mudah.",
            Icons.Default.Favorite
        ),
        OnboardingPage(
            "Catat Kesehatan Rutin \uD83D\uDCDD",
            "Masukkan data penting seperti Tekanan Darah, Gula Darah, dan Berat Badan. Semua tersimpan rapi untuk dipantau kapan saja.",
            Icons.Default.Create
        ),
        OnboardingPage(
            "Satu Aplikasi, Sekeluarga \uD83D\uDC6A",
            "Tambahkan profil Kakek, Nenek, Ayah, Ibu, hingga Anak. Masing-masing memiliki buku catatan kesehatannya sendiri.",
            Icons.Default.Person
        ),
        OnboardingPage(
            "Pengingat Pintar ⏰",
            "Sering lupa minum obat atau jadwal ke dokter? Pasang alarm pengingat di sini agar selalu tepat waktu.",
            Icons.Default.Notifications
        ),
        OnboardingPage(
            "Baca Artikel Sehat \uD83D\uDCD6",
            "Dapatkan berbagai tips dan informasi kesehatan terpercaya agar keluarga Anda tetap bugar dan ceria setiap hari.",
            Icons.Default.List
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    val brandColor = Color(0xFF10B981)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { position ->
            PagerScreen(onBoardingPage = pages[position], brandColor = brandColor)
        }

        Row(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) brandColor else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(if (pagerState.currentPage == iteration) 12.dp else 8.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = brandColor)
                ) { Text("Kembali") }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (pagerState.currentPage == pages.size - 1) {
                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(containerColor = brandColor)
                ) { Text("Mulai Sekarang!") }
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = brandColor)
                ) { Text("Lanjut") }
            }
        }
    }
}

@Composable
fun PagerScreen(onBoardingPage: OnboardingPage, brandColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = onBoardingPage.icon,
            contentDescription = "Icon Tutorial",
            modifier = Modifier.size(120.dp),
            tint = brandColor
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = onBoardingPage.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = onBoardingPage.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
