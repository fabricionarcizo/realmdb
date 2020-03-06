package dk.itu.moapd.realmdb;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.Sort;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainFragment extends Fragment {

    private EditText mEditText;

    private View mPrevSelected;
    private ActorName mActorName;

    private Realm mRealm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mRealm = Realm.getDefaultInstance();

        mEditText = view.findViewById(R.id.edit_text);

        Button insertButton = view.findViewById(R.id.insert_button);
        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(@NonNull Realm realm) {
                        String text = mEditText.getText().toString();
                        if (!text.isEmpty()) {
                            Number id = realm.where(ActorName.class).max("id");
                            if (id == null) id = 0;
                            ActorName actorName = new ActorName(id.intValue()+1, text);
                            realm.copyToRealm(actorName);
                            mEditText.setText("");
                            mActorName = null;
                        }
                    }
                });
            }
        });

        Button updateButton = view.findViewById(R.id.update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int id = (mActorName != null) ? mActorName.getId() : -1;
                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(@NonNull Realm realm) {
                        String text = mEditText.getText().toString();
                        if (!text.isEmpty() && id != -1) {
                            ActorName actorName = realm.where(ActorName.class)
                                    .equalTo("id", id).findFirst();
                            assert actorName != null;
                            actorName.setName(text);
                            realm.copyToRealm(actorName);
                            mEditText.setText("");
                            mActorName = null;
                        }
                    }
                });
            }
        });

        Button deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int id = (mActorName != null) ? mActorName.getId() : -1;
                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(@NonNull Realm realm) {
                        if (id != -1) {
                            ActorName actorName = realm.where(ActorName.class)
                                    .equalTo("id", id).findFirst();
                            assert actorName != null;
                            actorName.deleteFromRealm();
                            mEditText.setText("");
                            mActorName = null;
                        }
                    }
                });
            }
        });

        RealmResults<ActorName> results =
                mRealm.where(ActorName.class)
                      .sort("id", Sort.ASCENDING)
                      .findAllAsync();
        ActorNameAdapter mAdapter = new ActorNameAdapter(results);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    private class ActorNameAdapter
            extends RealmRecyclerViewAdapter<ActorName, ActorNameHolder> {


        ActorNameAdapter(@Nullable OrderedRealmCollection<ActorName> data) {
            super(data, true);
        }

        @NonNull
        @Override
        public ActorNameHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_actor_name, parent, false);
            return new ActorNameHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ActorNameHolder holder, int position) {
            final ActorName actorName = getItem(position);
            assert actorName != null;
            holder.mActorNameView.setText(actorName.getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String text = actorName.getName();
                    mEditText.setText(text);
                    mActorName = actorName;

                    if (mPrevSelected != null)
                        mPrevSelected.setBackgroundColor(view.getDrawingCacheBackgroundColor());
                    view.setBackgroundColor(Color.parseColor("#EEEEEE"));

                    mPrevSelected = view;
                }
            });
        }

    }

    private class ActorNameHolder extends RecyclerView.ViewHolder {

        private final TextView mActorNameView;

        ActorNameHolder(View view) {
            super(view);
            mActorNameView = view.findViewById(R.id.actor_name);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRealm != null)
            mRealm.close();
    }

}
