package dk.itu.moapd.realmdb;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.realm.Realm;
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

    private ActorNameAdapter mAdapter;
    private int mId;

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
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(@NonNull Realm realm) {
                        String text = mEditText.getText().toString();
                        if (!text.isEmpty()) {
                            Number id = realm.where(ActorName.class).max("id");
                            if (id == null) id = 0;
                            ActorName actorName = new ActorName(id.intValue()+1, text);
                            realm.copyToRealm(actorName);
                            mEditText.setText("");
                        }
                    }
                });

                updateUI();
            }
        });

        Button updateButton = view.findViewById(R.id.update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(@NonNull Realm realm) {
                        String text = mEditText.getText().toString();
                        if (!text.isEmpty()) {
                            ActorName actorName = realm.where(ActorName.class)
                                    .equalTo("id", mId).findFirst();
                            if (actorName != null) {
                                actorName.setName(text);
                                realm.copyToRealm(actorName);
                                mEditText.setText("");
                            }
                        }
                    }
                });

                updateUI();
            }
        });

        Button deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(@NonNull Realm realm) {
                        ActorName actorName = realm.where(ActorName.class)
                                .equalTo("id", mId).findFirst();
                        if (actorName != null) {
                            actorName.deleteFromRealm();
                            mEditText.setText("");
                        }
                    }
                });

                updateUI();
            }
        });

        mAdapter = new ActorNameAdapter();
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);

        updateUI();

        return view;
    }

    private void updateUI() {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                RealmResults<ActorName> results =
                        realm.where(ActorName.class)
                             .sort("id", Sort.ASCENDING)
                             .findAll();
                mAdapter.setActorNames(results);
            }
        });

        mId = -1;
    }

    private class ActorNameAdapter
            extends RecyclerView.Adapter<ActorNameHolder> {

        private RealmResults<ActorName> mActorNames;

        @NonNull
        @Override
        public ActorNameHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater =
                    LayoutInflater.from(parent.getContext());
            return new ActorNameHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ActorNameHolder holder, int position) {
            if (mActorNames != null && mActorNames.size() > position) {
                ActorName actorName = mActorNames.get(position);
                assert actorName != null;
                holder.bind(actorName);
            }
        }

        @Override
        public int getItemCount() {
            if (mActorNames != null)
                return mActorNames.size();
            return 0;
        }

        void setActorNames(RealmResults<ActorName> actorNames) {
            mActorNames = actorNames;
            notifyDataSetChanged();
        }

    }

    private class ActorNameHolder
            extends RecyclerView.ViewHolder {

        private int id;
        private final TextView mActorNameView;

        ActorNameHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_actor_name, parent, false));
            mActorNameView = itemView.findViewById(R.id.actor_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String text = mActorNameView.getText().toString();
                    mEditText.setText(text);
                    mId = id;

                    if (mPrevSelected != null)
                        mPrevSelected.setBackgroundColor(view.getDrawingCacheBackgroundColor());
                    view.setBackgroundColor(Color.parseColor("#EEEEEE"));

                    mPrevSelected = view;
                }
            });
        }

        void bind(ActorName actorName) {
            id = actorName.getId();
            mActorNameView.setText(actorName.getName());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRealm != null)
            mRealm.close();
    }

}
