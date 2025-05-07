package com.pineda.auxilio

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pineda.auxilio.databinding.ActivityRegisterBinding
import java.util.HashMap

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración del View Binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de EdgeToEdge
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.register) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(name, email, password)) {
                registerUser(name, email, password)
            }
        }

        binding.btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(name: String, email: String, password: String): Boolean {
        if (name.isEmpty()) {
            binding.etName.error = "El nombre es requerido"
            binding.etName.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "El correo es requerido"
            binding.etEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Por favor ingresa un correo válido"
            binding.etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "La contraseña es requerida"
            binding.etPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            binding.etPassword.requestFocus()
            return false
        }

        return true
    }

    private fun registerUser(name: String, email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Registro exitoso
                    val user = auth.currentUser

                    // Guardar información adicional en Realtime Database
                    saveUserData(user?.uid, name, email)

                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                    // Redirigir a la actividad principal
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Si falla el registro
                    Toast.makeText(
                        this,
                        "Error al registrarse: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserData(userId: String?, name: String, email: String) {
        userId?.let {
            val userRef = database.getReference("users").child(userId)

            val user = HashMap<String, Any>()
            user["name"] = name
            user["email"] = email
            user["createdAt"] = System.currentTimeMillis()

            userRef.setValue(user)
                .addOnSuccessListener {
                    // Datos guardados exitosamente
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al guardar datos: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
