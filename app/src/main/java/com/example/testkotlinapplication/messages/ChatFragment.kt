package com.example.testkotlinapplication.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.testkotlinapplication.R
import com.example.testkotlinapplication.databinding.FragmentChatBinding
import com.example.testkotlinapplication.models.Message
import com.example.testkotlinapplication.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_item_mate.view.*
import kotlinx.android.synthetic.main.chat_item_mate.view.textView
import kotlinx.android.synthetic.main.chat_item_you.view.*

class ChatFragment : Fragment() {

    private var binding: FragmentChatBinding? = null
    private var recyclerView: RecyclerView? = null
    private var sendButton: ImageButton? = null
    private var user: User? = null
    private lateinit var editText: EditText
    private lateinit var senderUid: String
    private lateinit var receiverUid: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding?.recyclerView
        sendButton = binding?.sendBtn
        user = requireActivity().intent.getParcelableExtra("KEY")
        editText = binding?.editText!!
        senderUid = Firebase.auth.currentUser!!.uid
        receiverUid = user!!.uid

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_chatFragment_to_messagesFragment)
                }
            })

        sendButton!!.setOnClickListener {
            /*
            вызывается база данных, создается обЪект сообщения фром ид -
            из пользователя в данный момент, ту ид - из ид пользователя из юзер айтема
            который был нажат. текст для сообщения берется из textview.
            обьект message отправляется в базу данных
             */


            val ref = Firebase.database("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/user-messages/${senderUid}/${receiverUid}").push() //вызов базы данных
            val refReceiver = Firebase.database("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/user-messages/${receiverUid}/${senderUid}").push()

            val latestMessagesRef = Firebase.database("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/latest-messages/${senderUid}/${receiverUid}")
            val latestMessagesRefReversed = Firebase.database("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/latest-messages/${receiverUid}/${senderUid}")

            val message = Message(Firebase.auth.currentUser!!.uid, user!!.uid, editText.text.toString(), ref.key!!) //создание обьекта message

            ref.setValue(message) // добавление обьекта message в бд
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Отправлено", Toast.LENGTH_SHORT).show()
                    editText.setText("")
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Ошибка: ${it.message}", Toast.LENGTH_SHORT).show()
                }

            refReceiver.setValue(message)

            latestMessagesRef.setValue(message)
            latestMessagesRefReversed.setValue(message)
        }

        fetchAdapterItems(senderUid, receiverUid)

        requireActivity().title = user?.name
    }

    private fun fetchAdapterItems(senderUid: String, receiverUid: String) {
        val adapter = GroupAdapter<GroupieViewHolder>()

        val ref = Firebase.database("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/user-messages/${senderUid}/${receiverUid}")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val newChatMessage = snapshot.getValue(Message::class.java)

                if (newChatMessage?.senderUid == Firebase.auth.currentUser?.uid) {
                    adapter.add(ChatItemYou(newChatMessage!!, MessagesFragment.currentUser!!))
                    recyclerView?.scrollToPosition(adapter.itemCount - 1)
                } else {
                    adapter.add(ChatItemMate(newChatMessage!!, user!!))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        recyclerView?.adapter = adapter
    }

    class ChatItemMate(private val message: Message, private val user: User) : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.textView.setText(message.text)
            Picasso.get().load(user.profilePhoto).into(viewHolder.itemView.mateImageView)
        }

        override fun getLayout(): Int {
            return R.layout.chat_item_mate
        }
    }

    class ChatItemYou(val message: Message, val user: User) : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.textView.setText(message.text)
            Picasso.get().load(user.profilePhoto).into(viewHolder.itemView.yourImageView)
        }

        override fun getLayout(): Int {
            return R.layout.chat_item_you
        }
    }
}
