package dk.itu.moapd.realmdb

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    private var mPrevSelected: View? = null
    private var mActorName: ActorName? = null

    private lateinit var mRealm: Realm

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        mRealm = Realm.getDefaultInstance()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        insert_button.setOnClickListener {
            mRealm.executeTransactionAsync { realm ->
                val text = edit_text.text.toString()
                if (text.isNotEmpty()) {
                    var id = realm.where(ActorName::class.java).max("id")
                    if (id == null) id = 0
                    val actorName = ActorName(id.toInt()+1, text)
                    realm.copyToRealm(actorName)
                    edit_text.setText("")
                    mActorName = null
                }
            }
        }

        update_button.setOnClickListener {
            val id = if (mActorName != null) mActorName?.id else -1
            mRealm.executeTransactionAsync { realm ->
                val text = edit_text.text.toString()
                if (text.isNotEmpty() && id != -1) {
                    val actorName = realm.where(ActorName::class.java)
                        .equalTo("id", id).findFirst()
                    actorName?.name = text
                    realm.copyToRealm(actorName!!)
                    edit_text.setText("")
                    mActorName = null
                }
            }
        }

        delete_button.setOnClickListener {
            val id = if (mActorName != null) mActorName?.id else -1
            mRealm.executeTransactionAsync { realm ->
                if (id != -1) {
                    val actorName = realm.where(ActorName::class.java)
                        .equalTo("id", id).findFirst()
                    actorName?.deleteFromRealm()
                    edit_text.setText("")
                    mActorName = null
                }
            }
        }

        val results = mRealm.where(ActorName::class.java)
            .sort("id", Sort.ASCENDING).findAllAsync()

        recycler_view.layoutManager = LinearLayoutManager(activity)
        recycler_view.adapter = ActorNameAdapter(results)
    }

    private inner class ActorNameAdapter(data: OrderedRealmCollection<ActorName>) :
            RealmRecyclerViewAdapter<ActorName, ActorNameHolder>(data, true) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorNameHolder {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.list_actor_name, parent, false)
            return ActorNameHolder(view)
        }

        override fun onBindViewHolder(holder: ActorNameHolder, position: Int) {
            val actorName = getItem(position)
            holder.mActorName.text = actorName?.name
            holder.itemView.setOnClickListener {
                mActorName = actorName
                edit_text.setText(actorName?.name)

                if (mPrevSelected != null)
                    mPrevSelected?.setBackgroundColor(it.solidColor)

                it.setBackgroundColor(Color.parseColor("#EEEEEE"))
                mPrevSelected = it
            }
            mPrevSelected?.setBackgroundColor(view?.solidColor!!)
        }

    }

    private inner class ActorNameHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val mActorName: TextView = view.findViewById(R.id.actor_name)

    }

}
