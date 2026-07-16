package com.example.jagasehat.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.jagasehat.model.Article
import com.example.jagasehat.viewmodel.AppViewModel

@Composable
fun ArticlesScreen(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()
    val user = state.currentUser
    val context = LocalContext.current

    val isAdmin = user?.role == "Admin"

    var showDialog by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }
    var currentArticleId by remember { mutableStateOf<String?>(null) }

    var titleInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("") }
    var authorInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var articleToDelete by remember { mutableStateOf<Article?>(null) }

    var articleToRead by remember { mutableStateOf<Article?>(null) }

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = {
                        editMode = false
                        titleInput = ""; categoryInput = ""; authorInput = ""; contentInput = ""
                        showDialog = true
                    },
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(28.dp))
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Artikel Kesehatan", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Text("Informasi dan panduan kesehatan terkini", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

            if (state.articles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = Color(0xFFF1F5F9), modifier = Modifier.size(80.dp)) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.padding(20.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada artikel", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 16.sp)
                        Text("Artikel yang diunggah akan tampil di sini", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(state.articles) { article ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { articleToRead = article },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column {
                                val displayUri = article.imageUri?.let { Uri.parse(it) }

                                if (displayUri != null) {
                                    AsyncImage(
                                        model = displayUri,
                                        contentDescription = "Banner",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxWidth().height(160.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(140.dp).background(Color(0xFFDCFCE7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF10B981).copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                                    }
                                }

                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = article.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp)) {
                                                Text(text = article.category, color = Color(0xFF16A34A), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                                            }
                                        }
                                        if (isAdmin) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Surface(shape = CircleShape, color = Color(0xFFEFF6FF), modifier = Modifier.size(36.dp)) {
                                                    IconButton(onClick = {
                                                        editMode = true
                                                        currentArticleId = article.id
                                                        titleInput = article.title
                                                        categoryInput = article.category
                                                        authorInput = article.author
                                                        contentInput = article.content
                                                        showDialog = true
                                                    }) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp)) }
                                                }
                                                Surface(shape = CircleShape, color = Color(0xFFFEF2F2), modifier = Modifier.size(36.dp)) {
                                                    IconButton(onClick = {
                                                        articleToDelete = article
                                                        showDeleteDialog = true
                                                    }) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(text = article.content, fontSize = 14.sp, color = Color.Gray, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(color = Color(0xFFF1F5F9))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Oleh: ${article.author}", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text(article.createdAt, fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (articleToRead != null) {
        val displayUri = articleToRead!!.imageUri?.let { Uri.parse(it) }

        AlertDialog(
            onDismissRequest = { articleToRead = null },
            title = {
                Text(text = articleToRead!!.title, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), fontSize = 22.sp)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (displayUri != null) {
                        AsyncImage(
                            model = displayUri,
                            contentDescription = "Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = articleToRead!!.category,
                            color = Color(0xFF16A34A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = articleToRead!!.content,
                        fontSize = 15.sp,
                        color = Color.DarkGray,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Ditulis oleh: ${articleToRead!!.author}\nDipublikasikan: ${articleToRead!!.createdAt}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { articleToRead = null },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) { Text("Tutup Artikel", fontWeight = FontWeight.Bold) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showDialog) {
        var imageUri by remember { mutableStateOf<Uri?>(null) }

        LaunchedEffect(currentArticleId) {
            if (editMode) {
                val saved = state.articles.find { it.id == currentArticleId }?.imageUri
                imageUri = saved?.let { Uri.parse(it) }
            }
        }

        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {
                }
                imageUri = uri
            }
        }

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF10B981),
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF1F5F9)
        )

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Surface(shape = CircleShape, color = Color(0xFFDCFCE7), modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.padding(16.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (editMode) "Edit Artikel" else "Tulis Artikel Baru",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .clickable { launcher.launch(arrayOf("image/*")) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Banner Artikel",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Klik untuk unggah gambar", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = titleInput, onValueChange = { titleInput = it },
                        placeholder = { Text("Judul Artikel", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, tint = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = categoryInput, onValueChange = { categoryInput = it },
                        placeholder = { Text("Kategori (ex: Gizi, Olahraga)", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = authorInput, onValueChange = { authorInput = it },
                        placeholder = { Text("Penulis (Default: Admin)", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = contentInput, onValueChange = { contentInput = it },
                        placeholder = { Text("Tulis isi artikel di sini...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(16.dp), colors = textFieldColors
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (titleInput.isNotBlank() && contentInput.isNotBlank()) {
                                val finalAuthor = authorInput.ifBlank { user?.name ?: "Admin" }

                                val imageValue = imageUri?.toString()
                                if (editMode && currentArticleId != null) {
                                    viewModel.updateArticle(currentArticleId!!, titleInput, categoryInput.ifBlank { "Umum" }, contentInput, finalAuthor, imageValue)
                                } else {
                                    viewModel.addArticle(titleInput, categoryInput.ifBlank { "Umum" }, contentInput, finalAuthor, imageValue)
                                }

                                showDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) { Text("Publikasikan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) }

                    TextButton(
                        onClick = { showDialog = false },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                }
            },
            dismissButton = null
        )
    }

    if (showDeleteDialog && articleToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Hapus Artikel?", fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444)) },
            text = { Text("Apakah Anda yakin ingin menghapus artikel '${articleToDelete?.title}'? Data yang dihapus tidak dapat dikembalikan.", color = Color.DarkGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteArticle(articleToDelete!!.id)
                        showDeleteDialog = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Ya, Hapus", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold) }
            }
        )
    }
}
