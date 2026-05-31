package com.example.pokedex.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pokedex.databinding.ActivityLoginBinding
import com.google.firebase.database.FirebaseDatabase
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val database = FirebaseDatabase.getInstance("https://pokedex-e01de-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")

    // Khai báo thêm Firebase Auth để xử lý Token Google cấp quyền lên Firebase
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Kiểm tra trạng thái đã đăng nhập lưu trong máy bằng SharedPreferences từ trước chưa
        val sharedPreferences = getSharedPreferences("PokedexPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            navigateToMain()
            return
        }

        // 2. Nếu chưa đăng nhập thì mới hiển thị giao diện nhập tài khoản
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sự kiện click nút LOGIN thủ công (GIỮ NGUYÊN)
        binding.btnLogin.setOnClickListener {
            handleUsernamePasswordLogin()
        }

        // SỰ KIỆN CLICK NÚT ĐĂNG NHẬP GOOGLE
        binding.btnGoogleLogin.setOnClickListener {
            loginWithGoogle()
        }

        // Chuyển sang màn hình Đăng ký khi người dùng chưa có tài khoản (GIỮ NGUYÊN)
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Logic kiểm tra và đối chiếu cặp tài khoản mật khẩu từ Firebase Database
     */
    private fun handleUsernamePasswordLogin() {
        val username = binding.edtUsername.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()

        if (username.isEmpty()) {
            binding.edtUsername.error = "Vui lòng nhập Username"
            binding.edtUsername.requestFocus()
            return
        }
        if (password.isEmpty()) {
            binding.edtPassword.error = "Vui lòng nhập Mật khẩu"
            binding.edtPassword.requestFocus()
            return
        }
        binding.btnLogin.isEnabled = false
        Toast.makeText(this, "Đang kiểm tra...", Toast.LENGTH_SHORT).show()

        // tìm kiếm tài khoản trong database
        database.child(username).get().addOnSuccessListener { snapshot ->

            binding.btnLogin.isEnabled = true
            if (snapshot.exists()) {
                // Nếu tồn tại tên đăng nhập -> Lấy mật khẩu thực tế đang lưu trên Firebase về
                val realPassword = snapshot.child("password").value.toString()

                if (password == realPassword) {
                    // Mật khẩu đúng -> Đăng nhập thành công
                    Toast.makeText(this, "Chào mừng quay trở lại, Trainer $username!", Toast.LENGTH_SHORT).show()

                    // Lưu trạng thái đăng nhập vào máy vĩnh viễn
                    val sharedPreferences = getSharedPreferences("PokedexPrefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit {
                        putBoolean("isLoggedIn", true)
                        putString(
                            "currentUsername",
                            username
                        ) // Lưu lại tên để sử dụng cho các tính năng khác
                    }

                    navigateToMain()
                } else {
                    // Sai mật khẩu
                    binding.edtPassword.error = "Mật khẩu không chính xác!"
                    binding.edtPassword.requestFocus()
                }
            } else {
                // Không tìm thấy cụm Username này trên Database
                binding.edtUsername.error = "Tài khoản không tồn tại!"
                binding.edtUsername.requestFocus()
            }
        }.addOnFailureListener { exception ->
            binding.btnLogin.isEnabled = true
            Toast.makeText(this, "Lỗi kết nối mạng: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Xử lý Logic Đăng nhập bằng Google thế hệ mới (Credential Manager)
     */
    private fun loginWithGoogle() {
        val credentialManager = androidx.credentials.CredentialManager.create(this)

        // Cấu hình Google ID Option với ID Web Client của bạn
        val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Hiện tất cả tài khoản Gmail có trên thiết bị để lựa chọn
            .setServerClientId("425909911029-p5r98uprkekta1ghdavmkcvtuccidppq.apps.googleusercontent.com")
            .setAutoSelectEnabled(false)
            .build()

        val request = androidx.credentials.GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Khởi chạy Coroutine để xử lý đồng bộ luồng mạng của Google API
        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@LoginActivity, request)
                handleGoogleSignInResult(result)
            } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                android.util.Log.e("GoogleLoginError", "Mã lỗi hệ thống: ${e.message}", e)
                Toast.makeText(this@LoginActivity, "Lỗi hệ thống: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     *  Lấy Token từ Credential Manager đổi mã gửi lên Firebase Auth xác thực vĩnh viễn
     */
    private fun handleGoogleSignInResult(result: androidx.credentials.GetCredentialResponse) {
        val credential = result.credential

        if (credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Đổi token lấy từ Google đưa vào cho Firebase xác thực
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val googleUser = auth.currentUser
                            val displayName = googleUser?.displayName ?: "Google Trainer"

                            Toast.makeText(this, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show()

                            // Đồng bộ lưu trạng thái đăng nhập vào SharedPrefs giống luồng gõ tay của bạn
                            val sharedPreferences = getSharedPreferences("PokedexPrefs", Context.MODE_PRIVATE)
                            sharedPreferences.edit {
                                putBoolean("isLoggedIn", true)
                                putString("currentUsername", displayName)
                            }

                            navigateToMain()
                        } else {
                            Toast.makeText(this, "Lỗi xác thực cổng kết nối Firebase!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "Lỗi xử lý tài khoản Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}