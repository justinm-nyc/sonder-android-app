package com.android.sonder_app.Fragment

import Adapter.UserAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sonder_app.Model.User

import com.android.sonder_app.R
import com.google.firebase.database.*

class SearchFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var mUsers: ArrayList<User>
    private lateinit var search_bar: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       var view: View = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        search_bar = view.findViewById(R.id.search_bar);
        mUsers = ArrayList<User>()

        userAdapter = UserAdapter(context!!, mUsers, true);
        recyclerView.adapter = userAdapter;

        readUsers()
        search_bar.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString().toLowerCase())
            }

        })

        return view
    }

    private fun searchUsers(s: String){
        var query: Query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username")
            .startAt(s)
            .endAt(s+"\uf8ff")

        query.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers.clear()
                for(snapshot in dataSnapshot.children){
                    var user: User? = snapshot.getValue(User::class.java)
                    mUsers
                }
                userAdapter.notifyDataSetChanged()
            }

        })
    }

    private fun readUsers() {
        var reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(search_bar.getText().toString().equals("")){
                    mUsers.clear()

                    for(snapshot in dataSnapshot.children){
                        var user: User = snapshot.getValue(User::class.java)!!
                        mUsers.add(user)
                    }
                    userAdapter.notifyDataSetChanged()

                }
            }

        })
    }
}
