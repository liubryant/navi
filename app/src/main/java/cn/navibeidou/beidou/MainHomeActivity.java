package cn.navibeidou.beidou;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import cn.navibeidou.beidou.translucentparent.StatusNavUtils;


public class MainHomeActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private MenuItem menuItem;
    private BottomNavigationView bottomNavigationView;


    boolean smoothScroll = false;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.tab_loc:
                    viewPager.setCurrentItem(0, smoothScroll);
                    //mSimpleToolbar.setMainTitle(item.getTitle().toString());
                    return true;
                case R.id.tab_option:
                    viewPager.setCurrentItem(2, smoothScroll);
                    //mSimpleToolbar.setMainTitle(item.getTitle().toString());
                    return true;
            }
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.i("navi", "activity_home  onCreate");
        StatusNavUtils.setStatusBarColor(this, 0x33000000);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        menuItem = bottomNavigationView.getMenu().getItem(0);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                menuItem = bottomNavigationView.getMenu().getItem(position);
                menuItem.setChecked(true);

                //mSimpleToolbar.setMainTitle(menuItem.getTitle().toString());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        setupViewPager(viewPager);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(MapFragment.newInstance((String) bottomNavigationView.getMenu().getItem(0).getTitle(), null));
//        adapter.addFragment(NewsFragment.newInstance((String) bottomNavigationView.getMenu().getItem(1).getTitle(), null));
        adapter.addFragment(OptionsFragment.newInstance((String) bottomNavigationView.getMenu().getItem(1).getTitle(), null));
//        adapter.addFragment(OptionsFragment.newInstance((String) bottomNavigationView.getMenu().getItem(2).getTitle(), null));
        viewPager.setAdapter(adapter);
    }
}


