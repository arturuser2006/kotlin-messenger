package com.example.testkotlinapplication.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testkotlinapplication.R
import com.example.testkotlinapplication.databinding.FragmentSecondBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SecondFragment : Fragment() {

    private lateinit var binding: FragmentSecondBinding
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var registrationTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSecondBinding.inflate(inflater, container, false)
        //binding.root возвращает тип данных view, открывается xml файл для данного фрагмента
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        email = binding.email
        password = binding.password
        loginButton = binding.loginBtn
        registrationTextView = binding.regTextView

        registrationTextView.setOnClickListener(View.OnClickListener {
            findNavController().navigate(R.id.action_secondFragment_to_firstFragment)
        })

        loginButton.setOnClickListener(View.OnClickListener {
            Firebase.auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Вы вошли как ${Firebase.auth.currentUser?.email}", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_secondFragment_to_messagesFragment)
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Ошибка: ${it.message}", Toast.LENGTH_LONG).show()
                }
        })

        activity?.setTitle("Войти")
    }
}