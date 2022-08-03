package com.example.testkotlinapplication.messages

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.testkotlinapplication.R
import com.example.testkotlinapplication.databinding.FragmentSelectUserBinding
import com.example.testkotlinapplication.models.User
import com.google.firebase.auth.ktx.auth
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

class SelectUserFragment : Fragment() {

    private var binding: FragmentSelectUserBinding? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSelectUserBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding?.recyclerView

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_selectUserFragment_to_messagesFragment)
                }
            })

        activity?.setTitle("Выберите пользователя")

        fetchUsers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.custom_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sign_out -> {
                Firebase.auth.signOut()
                findNavController().navigate(R.id.action_selectUserFragment_to_firstFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchUsers() {

        val ref = Firebase.database("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app").getReference("/users")
        val adapter = GroupAdapter<GroupieViewHolder>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    var userItem: UserItem = UserItem()

                    if (user != null) {
                        userItem.user = user
                    }
                    adapter.add(userItem)
                }

                adapter.setOnItemClickListener { item, view ->

                    val userItem = item as UserItem
                    requireActivity().intent.putExtra("KEY", userItem.user)

                    findNavController().navigate(R.id.action_selectUserFragment_to_chatFragment)
                }
                recyclerView!!.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    class UserItem: Item<GroupieViewHolder>() {

        var user: User? = null

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            var textView: TextView = viewHolder.itemView.findViewById(R.id.username)
            var imageView: CircleImageView = viewHolder.itemView.findViewById(R.id.profileImage)
            textView.text = user?.name

            Picasso.get().load(user?.profilePhoto).into(imageView)
        }

        override fun getLayout(): Int {
            return R.layout.user_item
        }
    }
}


