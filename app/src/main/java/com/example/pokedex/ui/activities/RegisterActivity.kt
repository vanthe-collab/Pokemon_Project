package com.example.pokedex.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pokedex.databinding.ActivityRegisterBinding
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    // 🌟 DÁN CÁI LINK TRONG ẢNH VÀO ĐÂY, ĐỪNG THIẾU CHỮ NÀO NHÉ:
    private val database = FirebaseDatabase.getInstance("https://pokedex-e01de-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            handleUsernamePasswordRegister()
        }

        binding.tvLoginRedirect.setOnClickListener {
            finish()
        }
    }

    private fun handleUsernamePasswordRegister() {
        val username = binding.edtUsername.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()
        val confirmPassword = binding.edtConfirmPassword.text.toString().trim()

        // 1. Kiểm tra Validate dữ liệu đầu vào
        if (username.isEmpty()) {
            binding.edtUsername.error = "Vui lòng nhập Username"
            binding.edtUsername.requestFocus()
            return
        }

        if (username.contains(" ")) {
            binding.edtUsername.error = "Username không được chứa khoảng trắng"
            binding.edtUsername.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.edtPassword.error = "Vui lòng nhập mật khẩu"
            binding.edtPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.edtPassword.error = "Mật khẩu phải từ 6 ký tự trở lên"
            binding.edtPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            binding.edtConfirmPassword.error = "Mật khẩu nhập lại không trùng khớp"
            binding.edtConfirmPassword.requestFocus()
            return
        }

        // Hiện Toast thông báo để chắc chắn hàm đã chạy qua bước kiểm tra
        Toast.makeText(this, "Đang kết nối máy chủ Firebase...", Toast.LENGTH_SHORT).show()
        binding.btnRegister.isEnabled = false

        // 2. Tiến hành đọc ghi dữ liệu trên Firebase bằng cấu hình AddOnCompleteListener để bao quát lỗi
        database.child(username).get().addOnCompleteListener { task ->
            // Mở lại trạng thái nút bấm khi nhận được phản hồi
            binding.btnRegister.isEnabled = true

            if (task.isSuccessful) {
                val snapshot = task.result
                if (snapshot != null && snapshot.exists()) {
                    binding.edtUsername.error = "Tên đăng nhập này đã có người sử dụng!"
                    binding.edtUsername.requestFocus()
                } else {
                    // Tiến hành tạo tài khoản mới nếu chưa tồn tại
                    database.child(username).child("password").setValue(password)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Đăng ký thành công Trainer $username!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseError", "Lỗi ghi dữ liệu: ${e.message}", e)
                            Toast.makeText(this, "Lỗi ghi Firebase: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            } else {
                // In lỗi trực tiếp ra màn hình và Logcat nếu bị Firebase chặn quyền (Permission Denied)
                val exception = task.exception
                Log.e("FirebaseError", "Firebase từ chối kết nối: ${exception?.message}", exception)
                Toast.makeText(this, "Firebase lỗi: ${exception?.message ?: "Từ chối truy cập"}", Toast.LENGTH_LONG).show()
            }
        }
    }
}