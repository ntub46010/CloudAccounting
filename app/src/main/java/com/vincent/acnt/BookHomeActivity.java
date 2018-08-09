package com.vincent.acnt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import static com.vincent.acnt.data.MyApp.KEY_BOOK_NAME;
import static com.vincent.acnt.data.MyApp.KEY_CREATOR;

public class BookHomeActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;

    private DrawerLayout drawerLayout;
    private NavigationView navigation_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_home);
        context = this;
        Bundle bundle = getIntent().getExtras();

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(bundle.getString(KEY_BOOK_NAME));
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigation_view = findViewById(R.id.navigation_view);

        setupDrawer(bundle);
    }

    private void setupDrawer(Bundle bundle) {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);

        navigation_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // 點選時收起選單
                drawerLayout.closeDrawer(GravityCompat.START);

                switch (item.getItemId()) {
                    case R.id.action_journal:
                        break;
                    case R.id.action_ledger:
                        break;
                    case R.id.action_report:
                        break;
                    case R.id.action_subject:
                        break;
                }

                return false;
            }
        });

        View header = navigation_view.getHeaderView(0);
        TextView txtBookName = header.findViewById(R.id.txtBookName);
        TextView txtCreator = header.findViewById(R.id.txtCreator);
        txtBookName.setText(bundle.getString(KEY_BOOK_NAME));
        txtCreator.setText("由" + bundle.getString(KEY_CREATOR) + "建立");
    }
}
