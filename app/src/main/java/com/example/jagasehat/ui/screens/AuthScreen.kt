package com.example.jagasehat.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jagasehat.R
import com.example.jagasehat.viewmodel.AppViewModel

@Composable
fun AuthScreen(viewModel: AppViewModel) {
    var showLogin by remember { mutableStateOf(true) }

    if (showLogin) {
        LoginScreen(
            viewModel = viewModel,
            onNavigateToRegister = { showLogin = false },
            onLoginSuccess = { }
        )
    } else {
        RegisterScreen(
            viewModel = viewModel,
            onNavigateToLogin = { showLogin = true },
            onRegisterSuccess = { showLogin = true }
        )
    }
}

@Composable
fun GoogleAccountPickerDialog(
    onDismiss: () -> Unit,
    onAccountSelected: (String, String) -> Unit
) {
    var accounts by remember {
        mutableStateOf(
            listOf(
                Pair("raihan.pratama@gmail.com", "Raihan Pratama")
            )
        )
    }

    var isAddingNewAccount by remember { mutableStateOf(false) }

    var newNameInput by remember { mutableStateOf("") }
    var newEmailInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painter = painterResource(id = R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isAddingNewAccount) "Tambah Akun Google" else "Pilih akun",
                    fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.Black
                )
                if (!isAddingNewAccount) {
                    Text("untuk melanjutkan ke JagaSehat", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Divider(color = Color(0xFFF1F5F9))

                if (isAddingNewAccount) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newNameInput,
                        onValueChange = { newNameInput = it },
                        label = { Text("Nama Pengguna") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newEmailInput,
                        onValueChange = { newEmailInput = it },
                        label = { Text("Email Google") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            isAddingNewAccount = false
                            newNameInput = ""
                            newEmailInput = ""
                        }) {
                            Text("Batal", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newNameInput.isNotBlank() && newEmailInput.isNotBlank()) {
                                    accounts = accounts + Pair(newEmailInput, newNameInput)
                                    isAddingNewAccount = false
                                    newNameInput = ""
                                    newEmailInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Text("Simpan")
                        }
                    }
                } else {
                    accounts.forEach { account ->
                        val email = account.first
                        val name = account.second
                        val initial = name.firstOrNull()?.toString() ?: "U"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAccountSelected(email, name) }
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF3B82F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.DarkGray)
                                Text(email, fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                        Divider(color = Color(0xFFF1F5F9))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAddingNewAccount = true }
                            .padding(vertical = 16.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Tambahkan akun lain", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    var showGooglePicker by remember { mutableStateOf(false) }

    if (showGooglePicker) {
        GoogleAccountPickerDialog(
            onDismiss = { showGooglePicker = false },
            onAccountSelected = { email, name ->
                showGooglePicker = false
                viewModel.loginWithGoogle(context, email, name)
                onLoginSuccess()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text("Halo, JagaSehat!", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Silakan masuk untuk melanjutkan", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username", tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFE2E8F0))
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color.Gray) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(imageVector = image, contentDescription = null, tint = Color.Gray) }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFE2E8F0))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981)))
                Text("Ingat Saya", fontSize = 12.sp, color = Color.DarkGray)
            }
            Text("Lupa Password?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.clickable { })
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (username.isNotBlank() && password.isNotBlank()) {
                    val status = viewModel.loginUser(context, username, password)
                    if (status == "SUCCESS") {
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Username dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Masuk", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            Text(" Atau masuk dengan ", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { showGooglePicker = true },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Image(painter = painterResource(id = R.drawable.ic_google), contentDescription = "Logo Google", modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Lanjutkan dengan Google", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.padding(bottom = 32.dp, top = 24.dp)) {
            Text("Belum punya akun? ", fontSize = 14.sp, color = Color.Gray)
            Text("Daftar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.clickable { onNavigateToRegister() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AppViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var showGooglePicker by remember { mutableStateOf(false) }

    if (showGooglePicker) {
        GoogleAccountPickerDialog(
            onDismiss = { showGooglePicker = false },
            onAccountSelected = { userEmail, userName ->
                showGooglePicker = false
                viewModel.loginWithGoogle(context, userEmail, userName)
                onNavigateToLogin()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text("Buat Akun Baru", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Lengkapi data diri Anda di bawah ini", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nama Lengkap") }, leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "Name", tint = Color.Gray) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFE2E8F0)))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username", tint = Color.Gray) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFE2E8F0)))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Aktif") }, leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color.Gray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFE2E8F0)))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Nomor Telepon") }, leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone", tint = Color.Gray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFE2E8F0)))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color.Gray) }, trailingIcon = { val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff; IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(imageVector = image, contentDescription = null, tint = Color.Gray) } }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFE2E8F0)))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Konfirmasi Password") }, leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm Password", tint = Color.Gray) }, trailingIcon = { val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff; IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(imageVector = image, contentDescription = null, tint = Color.Gray) } }, visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFE2E8F0)))

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank() || fullName.isBlank()) { Toast.makeText(context, "Harap lengkapi semua data!", Toast.LENGTH_SHORT).show() }
                else if (password != confirmPassword) { Toast.makeText(context, "Password tidak cocok!", Toast.LENGTH_SHORT).show() }
                else {
                    val success = viewModel.registerUser(context, username, fullName, "User Biasa", password, email, phone)
                    if (success) { Toast.makeText(context, "Berhasil mendaftar! Silakan login.", Toast.LENGTH_LONG).show(); onRegisterSuccess() }
                    else { Toast.makeText(context, "Username sudah digunakan!", Toast.LENGTH_SHORT).show() }
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), shape = RoundedCornerShape(16.dp), elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) { Text("Daftar Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { showGooglePicker = true },
            modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Image(painter = painterResource(id = R.drawable.ic_google), contentDescription = "Logo Google", modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Lanjutkan dengan Google", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.padding(bottom = 32.dp)) {
            Text("Sudah punya akun? ", fontSize = 14.sp, color = Color.Gray)
            Text("Masuk", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.clickable { onNavigateToLogin() })
        }
    }
}
