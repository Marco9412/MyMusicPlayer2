package com.panni.mymusicplayer2.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumer;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.widgets.IntroductoryOverlay;
import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.fragments.FragmentController;
import com.panni.mymusicplayer2.controller.fragments.FragmentControllerListener;
import com.panni.mymusicplayer2.controller.fragments.StackFragmentController;
import com.panni.mymusicplayer2.controller.permission.PermissionChecker;
import com.panni.mymusicplayer2.controller.player.Player;
import com.panni.mymusicplayer2.view.fragments.BaseFragment;
import com.panni.mymusicplayer2.view.fragments.CustomSongListFragment;
import com.panni.mymusicplayer2.view.fragments.LocalSongListFragment;
import com.panni.mymusicplayer2.view.fragments.PlayerFragment;
import com.panni.mymusicplayer2.view.fragments.SearchFragment;
import com.panni.mymusicplayer2.view.fragments.SongListFragment;
import com.panni.mymusicplayer2.view.fragments.YoutubeFragment;
import com.panni.mymusicplayer2.view.settings.SettingsActivity;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FragmentControllerListener {

    private FragmentController fragmentController;
    private int currentFragmentType;

    private IntroductoryOverlay overlay;
    private MenuItem castItem;
    private VideoCastConsumer consumerOverlay;

    //private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Permissions in android 6
        new PermissionChecker(this).checkPermission();

        // Init controller!
        ControllerImpl.getInstance(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentController.newFragment(PlayerFragment.create());
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // First window is folders
        BaseFragment firstView;
        if (ControllerImpl.getInstance().getCurrentQueue().length() == 0) {
            firstView = SongListFragment.create();
            fragmentChanged(BaseFragment.TYPE_FOLDER);
        } else {
            firstView = PlayerFragment.create();
            fragmentChanged(BaseFragment.TYPE_PLAYER);
        }

        this.fragmentController = new StackFragmentController(getSupportFragmentManager(),
                R.id.framelayout, firstView);
        this.fragmentController.addFragmentControllerListener(this);

        showCastAvailability();

        // Swipe Gesture handler
        //gestureDetector = new GestureDetector(new SwipeGestureDetector());
    }

    private void showCastAvailability() {
        consumerOverlay = new VideoCastConsumerImpl() {
            @Override
            public void onCastAvailabilityChanged(boolean castPresent) {
                if (castPresent) showOverlay();
            }
        };
        VideoCastManager.getInstance().addVideoCastConsumer(consumerOverlay);
    }

    private void showOverlay() {
        if (overlay != null) overlay.remove();
        VideoCastManager.getInstance().removeVideoCastConsumer(consumerOverlay);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (castItem != null && castItem.isVisible()) {
                    overlay = new IntroductoryOverlay.Builder(MainActivity.this)
                            .setMenuItem(castItem)
                            .setTitleText("A chromecast has been found! You can play your music with it by tapping this icon!")
                            .setSingleTime()
                            .setOnDismissed(new IntroductoryOverlay.OnOverlayDismissedListener() {
                                @Override
                                public void onOverlayDismissed() {
                                    overlay = null;
                                }
                            })
                            .build();
                    overlay.show();
                }
            }
        }, 1000);
    }

    public FragmentController getFragmentController() {
        return fragmentController;
    }

//    public GestureDetector getGestureDetector() {
//        return gestureDetector;
//    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (currentFragmentType == BaseFragment.TYPE_YOUTUBE &&
                ((YoutubeFragment) this.fragmentController.getCurrent()).webViewBack()) {
            return;
        } else if (!this.fragmentController.back()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_player, menu);
        castItem = VideoCastManager.getInstance().addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            // Player menu
            case R.id.clear_queue:
                ControllerImpl.getInstance().getCurrentQueue().clear();
                ControllerImpl.getInstance().stopPlayers();
                return true;
            case R.id.shufflemenu:
                item.setChecked(!item.isChecked());
                ControllerImpl.getInstance().getCurrentQueue().setShuffle(item.isChecked());
                return true;
            case R.id.repeatmenu:
                item.setChecked(!item.isChecked());
                ControllerImpl.getInstance().getCurrentQueue().setRepeat(item.isChecked());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // TODO folder menu should keep last folder state!
        switch (id) {
            case R.id.nav_player:
                if (currentFragmentType != BaseFragment.TYPE_PLAYER)
                    this.fragmentController.newFragment(PlayerFragment.create());
                break;
            case R.id.nav_song:
                if (currentFragmentType != BaseFragment.TYPE_LOCAL_SONG)
                    this.fragmentController.reset(LocalSongListFragment.create());
                break;
            case R.id.nav_custom:
                if (currentFragmentType != BaseFragment.TYPE_CUSTOM_SONG)
                    this.fragmentController.reset(CustomSongListFragment.create());
                break;
            case R.id.nav_folder:
                if (currentFragmentType != BaseFragment.TYPE_FOLDER)
                    this.fragmentController.reset(SongListFragment.create());
                break;
            case R.id.nav_search:
                if (currentFragmentType != BaseFragment.TYPE_SEARCH)
                    this.fragmentController.reset(SearchFragment.create());
                break;
            case R.id.nav_youtube:
                if (currentFragmentType != BaseFragment.TYPE_YOUTUBE)
                    this.fragmentController.reset(YoutubeFragment.create());
                break;
            case R.id.nav_settings:
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
                break;
            case R.id.nav_quit:
                ControllerImpl.getInstance().quit();
                finish();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        VideoCastManager.getInstance().incrementUiCounter();
    }

    @Override
    protected void onPause() {
        super.onPause();

        VideoCastManager.getInstance().decrementUiCounter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        int currentState = ControllerImpl.getInstance().getCurrentPlayer().getCurrentState();

        ControllerImpl.getInstance().unloadService(this);

        if (currentState != Player.STATE_PLAYING)
            ControllerImpl.getInstance().quit();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return VideoCastManager.getInstance().onDispatchVolumeKeyEvent(event, 0.1f) || super.dispatchKeyEvent(event);
    }

    @Override
    public void fragmentChanged(int current) {
        this.currentFragmentType = current;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.getMenu().getItem(current).setChecked(true);

        if (current == BaseFragment.TYPE_PLAYER)
            fab.setVisibility(View.INVISIBLE);
        else
            fab.setVisibility(View.VISIBLE);
    }

//    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
//        private static final int SWIPE_MIN_DISTANCE = 50;
//        private static final int SWIPE_MAX_OFF_PATH = 200;
//        private static final int SWIPE_THRESHOLD_VELOCITY = 200;
//
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//                               float velocityY) {
//            try {
//                float diffAbs = Math.abs(e1.getY() - e2.getY());
//                float diff = e1.getX() - e2.getX();
//
//                if (diffAbs > SWIPE_MAX_OFF_PATH)
//                    return false;
//
//                // Left swipe
//                if (diff > SWIPE_MIN_DISTANCE
//                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    if (currentFragmentType == FragmentType.FOLDER_FRAGMENT)
//                        onBackPressed();
//                }
//                // Right swipe
//                //else if (-diff > SWIPE_MIN_DISTANCE
//                //        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                //    Home.this.onRightSwipe();
//                //}
//            } catch (Exception e) {
//                Log.e("Home", "Error on gestures");
//            }
//            return false;
//        }
//
//    }
}

