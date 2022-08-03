package com.example.testkotlinapplication.registration

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testkotlinapplication.R
import com.example.testkotlinapplication.models.User
import com.example.testkotlinapplication.databinding.FragmentFirstBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.util.*

class FirstFragment : Fragment() {

    private lateinit var binding: FragmentFirstBinding
    private lateinit var name: EditText
    private lateinit var password: EditText
    private lateinit var email: EditText
    private lateinit var regButton: Button
    private lateinit var accountExistsTextView: TextView
    private lateinit var photoButton: Button
    private var photoUri: Uri? = null
    private lateinit var circleImageView: CircleImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name = binding.name
        password = binding.password
        email = binding.email
        regButton = binding.registrationButton
        accountExistsTextView = binding.alreadyHaveAccountTextView
        photoButton = binding.photoButton
        circleImageView = binding.photoCircle

        regButton.setOnClickListener(View.OnClickListener {
            try {
                registerUser()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        accountExistsTextView.setOnClickListener(View.OnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_secondFragment)
        })

        photoButton.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        })

        activity?.setTitle("Регистрация")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        photoUri = data?.data
        val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, photoUri)
        photoButton.alpha = 0f; //делает кнопку выбора фото прозрачной
        circleImageView.setImageBitmap(bitmap)
    }

    private fun registerUser() {
        if (email.text.toString().isEmpty() || password.text.toString().isEmpty()) {
            Toast.makeText(requireContext(), "Заполните пустые поля", Toast.LENGTH_LONG).show()
            return
        }

        Firebase.auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnSuccessListener(OnSuccessListener {
                Toast.makeText(requireContext(), "Регистрация прошла успешно: ${it.user?.email}", Toast.LENGTH_LONG).show()
                uploadPhotoToFirebaseStorage()
            })
            .addOnFailureListener(OnFailureListener {
                Toast.makeText(requireContext(), "Ошибка: ${it.message}", Toast.LENGTH_LONG).show()
            })
    }

    private fun uploadPhotoToFirebaseStorage() {
        if (photoUri == null) {
            Toast.makeText(requireContext(), "Выберите фото профиля", Toast.LENGTH_SHORT).show()
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/${filename}")
        ref.putFile(photoUri!!)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Фото профиля добавлено успешно", Toast.LENGTH_LONG).show()
                ref.downloadUrl
                    .addOnSuccessListener {
                        uploadUserToFirebaseDatabase(it.toString())
                        Log.d("MyLog", "${it.toString()}")
                    }
                    .addOnFailureListener {
                        Log.d("MyLog", "${it.message}")
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка при загрузке фото", Toast.LENGTH_SHORT).show()
                Log.d("MyLog", "${it.message}")
            }

    }

    private fun uploadUserToFirebaseDatabase(photoPath: String) {
        val uid = FirebaseAuth.getInstance().uid
        val ref = Firebase.database("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app").getReference("/users/${uid}")
        val user: User = User(name.text.toString().trim(), password.text.toString().trim(), email.text.toString().trim(), uid!!, photoPath)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("MyLog", "Пользователь добавлен успешно в базу данных")
                findNavController().navigate(R.id.action_firstFragment_to_messagesFragment)
            }
            .addOnFailureListener {
                Log.d("MyLog", "Ошибка при добавлении пользователя в базу данных ${it.message}")
            }
    }
}