package com.example.testkotlinapplication.messages

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.testkotlinapplication.R
import com.example.testkotlinapplication.databinding.FragmentMessagesBinding
import com.example.testkotlinapplication.models.Message
import com.example.testkotlinapplication.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView

class MessagesFragment : Fragment() {

    private lateinit var binding: FragmentMessagesBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var map: HashMap<String, Message>

    companion object {
        var currentUser: User? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerView
        map = HashMap()

        verifyUserIsLoggedIn()
        fetchCurrentUser()
        fetchUserMessages()
        activity?.setTitle("Сообщения")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.custom_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.start_chat -> {
                findNavController().navigate(R.id.action_messagesFragment_to_selectUserFragment)
            }
            R.id.sign_out -> {
                Firebase.auth.signOut()
                findNavController().navigate(R.id.action_messagesFragment_to_firstFragment2)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun verifyUserIsLoggedIn() {
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            findNavController().navigate(R.id.action_messagesFragment_to_firstFragment2)
        }
    }

    private fun fetchCurrentUser() {
        val uid = Firebase.auth.currentUser?.uid
        var ref = Firebase.database("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app").getReference("/users/${uid}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.d("MyLog", "current user is: ${currentUser?.name}")
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun fetchUserMessages() {
        var adapter = GroupAdapter<GroupieViewHolder>()

        var ref = Firebase.database("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/latest-messages/${Firebase.auth.currentUser?.uid}")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                var message = snapshot.getValue(Message::class.java)

                map.put(snapshot.key!!, message!!)
                refreshAdapter(adapter)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                var message = snapshot.getValue(Message::class.java)

                map.put(snapshot.key!!, message!!)
                refreshAdapter(adapter)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        adapter.setOnItemClickListener { item, view ->
            var recentMessage: RecentMessage = item as RecentMessage
            var message: Message = recentMessage.message
            var partnerUid: String
            var user: User?

            if (message.senderUid == Firebase.auth.currentUser?.uid) {
                partnerUid = message.receiverUid
            } else {
                partnerUid = message.senderUid
            }

            var ref = Firebase.database("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/users")

            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        user = it.getValue(User::class.java)

                        if (user?.uid == partnerUid) {
                            requireActivity().intent.putExtra("KEY", user)
                            findNavController().navigate(R.id.action_messagesFragment_to_chatFragment)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        recyclerView.adapter = adapter
    }

    private fun refreshAdapter(adapter: GroupAdapter<GroupieViewHolder>) {
        adapter.clear()
        map.values.forEach {
            adapter.add(RecentMessage(it))
        }
    }

    class RecentMessage(val message: Message): Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            var partnerUid: String
            var user: User?

            if (message.senderUid == Firebase.auth.currentUser?.uid) {
                partnerUid = message.receiverUid
            } else {
                partnerUid = message.senderUid
            }

            var ref = Firebase.database("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/users")

            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        user = it.getValue(User::class.java)

                        if (user?.uid == partnerUid) {
                            viewHolder.itemView.findViewById<TextView>(R.id.usernameTextView).setText(user?.name)
                            Picasso.get().load(user?.profilePhoto).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.imageView))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

            viewHolder.itemView.findViewById<TextView>(R.id.messageText).setText("${message.text}")
        }

        override fun getLayout(): Int {
            return R.layout.user_item_messages_fragment
        }
    }
}

