package com.example.ecomer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth // Đã sửa lỗi khai báo biến

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

                if (!isLoggedIn) {
                    LoginScreen(
                        onLoginSuccess = { isLoggedIn = true }
                    )
                } else {
                    AdminDashboard(
                        onLogout = {
                            auth.signOut()
                            isLoggedIn = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "ĐĂNG NHẬP HỆ THỐNG", fontSize = 22.sp, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Tạo tài khoản & Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                } else {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ĐĂNG NHẬP / ĐĂNG KÝ")
        }
    }
}

data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val price: String = "",
    val image: String = ""
)

@Composable
fun AdminDashboard(onLogout: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    var editingProductId by remember { mutableStateOf<String?>(null) }
    var productList by remember { mutableStateOf(listOf<Product>()) }

    LaunchedEffect(key1 = Unit) {
        db.collection("products").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                productList = snapshot.documents.map { doc ->
                    Product(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        category = doc.getString("category") ?: "",
                        price = doc.getString("price") ?: "",
                        image = doc.getString("image") ?: ""
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hàng tiêu đề và nút đăng xuất đã được căn chỉnh chuẩn không bị vỡ giao diện
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quản lý sản phẩm",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f) // Giúp chữ tự co giãn, không lấn át nút đăng xuất
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("Đăng xuất")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên sản phẩm") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Loại sản phẩm") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        TextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Giá") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        TextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Đường dẫn ảnh (URL)") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))


        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Preview Image",
                modifier = Modifier
                    .size(90.dp)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))


        Button(
            onClick = {
                val productData = hashMapOf(
                    "name" to name,
                    "category" to category,
                    "price" to price,
                    "image" to imageUrl
                )

                if (editingProductId == null) {
                    db.collection("products").add(productData)
                        .addOnSuccessListener {
                            name = ""; category = ""; price = ""; imageUrl = ""
                        }
                } else {
                    db.collection("products").document(editingProductId!!).set(productData)
                        .addOnSuccessListener {
                            name = ""; category = ""; price = ""; imageUrl = ""
                            editingProductId = null
                        }
                }
            },
            enabled = name.isNotEmpty() && imageUrl.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (editingProductId == null) "THÊM SẢN PHẨM" else "CẬP NHẬT SẢN PHẨM")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Danh sách sản phẩm:",
            modifier = Modifier.align(Alignment.Start),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))


        for (product in productList) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = product.image,
                        contentDescription = "Product Image",
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Tên: ${product.name}")
                        Text(text = "Giá: ${product.price}")
                        Text(text = "Loại: ${product.category}")
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Button(
                            onClick = {
                                name = product.name
                                category = product.category
                                price = product.price
                                imageUrl = product.image
                                editingProductId = product.id
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.height(32.dp).padding(bottom = 4.dp)
                        ) {
                            Text("Sửa", color = Color.White)
                        }

                        Button(
                            onClick = {
                                if (product.id.isNotEmpty()) {
                                    db.collection("products").document(product.id).delete()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Xóa", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}