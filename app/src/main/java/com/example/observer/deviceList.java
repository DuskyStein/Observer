package com.example.observer;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class deviceList extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> listData;
    DatabaseReference mDataBase;
    private String DEVICE_KEY = "Devices" + "/" + MainActivity.locData.getDate();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        init();
        getDataFromDB();
    }
    private void init() {
    listView = findViewById(R.id.listView);
    listData = new ArrayList<>();
    adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
    listView.setAdapter(adapter);
    mDataBase = FirebaseDatabase.getInstance().getReference(DEVICE_KEY);

    }
private void getDataFromDB() {
    ValueEventListener vListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
        for (DataSnapshot ds : snapshot.getChildren()){
            LocationData devices = ds.getValue(LocationData.class);
            assert devices !=null;
            listData.add(devices.getDate());
        }
        adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    mDataBase.addValueEventListener(vListener);
}
}



