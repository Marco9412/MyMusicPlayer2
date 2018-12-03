package com.panni.mymusicplayer2.view.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.DataCallback;
import com.panni.mymusicplayer2.controller.fragments.FragmentController;
import com.panni.mymusicplayer2.model.queue.objects.CustomQueueItem;
import com.panni.mymusicplayer2.view.MainActivity;
import com.panni.mymusicplayer2.view.adapters.SongFolderAdapter;
import com.panni.mymusicplayer2.view.listeners.ClickOnMusicListViewListener;
import com.panni.mymusicplayer2.youtubedl.LinkGetter;
import com.panni.mymusicplayer2.youtubedl.NativePythonLinkGetter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import objects.DbObject;

/**
 * Created by marco on 15/05/16.
 */
public class YoutubeFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private FragmentController fragmentController;

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SongFolderAdapter adapter;


    public static BaseFragment create() {
        return new YoutubeFragment();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        /*AdapterView.AdapterContextMenuInfo infos = (AdapterView.AdapterContextMenuInfo) menuInfo;
        MenuInflater menuInflater = getActivity().getMenuInflater();

        DbObject current = adapter.getItem(infos.position);

        if (current instanceof Song) {
            menuInflater.inflate(R.menu.longclicklistsongmenu, menu);
            menu.setHeaderTitle(((Song) current).getTitle());
            menu.setHeaderIcon(Utils.mimeTypeToIconResource(((Song) current).getMimeType()));
        } else if (current instanceof Folder) {
            menuInflater.inflate(R.menu.longclickfoldermenu, menu);
            menu.setHeaderTitle(current.getName());
            menu.setHeaderIcon(R.drawable.ic_menu_folder);
        }*/
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        /*AdapterView.AdapterContextMenuInfo infos = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        DbObject current = adapter.getItem(infos.position);

        switch (item.getItemId()) {
            // Folder
            case R.id.enqueuefolder:
                ControllerImpl.getInstance().enqueueFolder((Folder)current, false, false);
                break;
            case R.id.enqueueplayfolder:
                Toast.makeText(getActivity(), "Not implemented yet!", Toast.LENGTH_SHORT).show();
                //ControllerImpl.getInstance().enqueueFolder((Folder)objects[position], false, true);
                break;
            case R.id.downloadfolder:
                ControllerImpl.getInstance().downloadFolder(getActivity(), (Folder)current);
                break;
            case R.id.getm3u: // TODO
                Toast.makeText(getActivity(), "Not implemented yet!", Toast.LENGTH_SHORT).show();
                break;

            // Song
            case R.id.enqueuesong:
                ControllerImpl.getInstance().getCurrentQueue().enqueue((Song)current);
                break;
            case R.id.enqueueplaysong:
                ControllerImpl.getInstance().getCurrentQueue().enqueue((Song)current);
                ControllerImpl.getInstance().getCurrentPlayer().play(ControllerImpl.getInstance().getCurrentQueue().length() - 1);
                break;
            case R.id.playextsong:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(ControllerImpl.getInstance().getHttpSongUrl((Song) current)), "audio/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
                break;
            case R.id.downloadsong:
                ControllerImpl.getInstance().downloadSong(getActivity(), (Song) current);
                break;
            case R.id.sharesong:
                ControllerImpl.getInstance().shareQueueItem(getActivity(), MyQueueItem.create((Song) current));
                break;
        }*/

        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO efficient method?
        return inflater.inflate(R.layout.youtube_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.fragmentController = ((MainActivity)getActivity()).getFragmentController();

        this.swipeRefreshLayout = getView().findViewById(R.id.swyperefreshsearchyt);
        this.swipeRefreshLayout.setOnRefreshListener(this);

        this.webView = getView().findViewById(R.id.youtubeWebView);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.loadUrl("https://www.youtube.com");
        this.webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                // Page loaded!
                YoutubeFragment.this.swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Page loaded!
                YoutubeFragment.this.swipeRefreshLayout.setRefreshing(false);
            }

            /*@Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }*/

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
               if ((request.getUrl().getHost().equals("www.youtube.com") ||
                        request.getUrl().getHost().equals("m.youtube.com")) &&
                        request.getUrl().getEncodedPath().equals("/watch")
                        ) {
                   // Trigger YT Download ?
                   if (new NativePythonLinkGetter().available(YoutubeFragment.this.getActivity())) {
                       getActivity().runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               Toast.makeText(getActivity(), "Loading in custom song...", Toast.LENGTH_LONG).show();
                               ControllerImpl.getInstance().saveCustomSong(new CustomQueueItem("Test", new NativePythonLinkGetter().getLink(request.getUrl().toString())));
                           }
                       });
                   }

                   YoutubeFragment.this.getActivity().runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           YoutubeFragment.this.webViewBack(); // Go to previous page (disables player!)
                       }
                   });

                   return new WebResourceResponse("text/html", "utf-8", 404, "Invalid", null, null);
               } else {
                   return super.shouldInterceptRequest(view, request);
               }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().getHost().equals("www.youtube.com") ||
                        request.getUrl().getHost().equals("m.youtube.com")) {
                    /*if (request.getUrl().getEncodedPath().equals("/watch")) {
                        // Trigger something!
                        return true;
                    }*/
                    // Let WebView load the page
                    return false;
                }

                // Load page externally
                Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        doYoutubeSearch(false, false);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean webViewBack() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }

    private void doYoutubeSearch(boolean force, boolean fromButton) {
        // TODO
        this.swipeRefreshLayout.setRefreshing(true);



        this.swipeRefreshLayout.setRefreshing(false);
    }

    /*public void newData(final DbObject[] objects) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
                swipeRefreshLayout.setRefreshing(false);

                View view = getView();
                if (view == null) return;

                ListView list = (ListView) view.findViewById(R.id.searchlistView);
                if (list == null) return;

                adapter = new SongFolderAdapter(getContext(), objects);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new ClickOnMusicListViewListener(fragmentController, objects));
                //list.setOnItemLongClickListener(new LongClickOnMusicListViewListener(getActivity(), objects));

                if (listViewState != null)
                    list.onRestoreInstanceState(listViewState);
            }
        });
    }*/

    @Override
    public void onRefresh() {
        // TODO think about it
        this.doYoutubeSearch(true, true);
    }

    @Override
    public int getType() {
        return BaseFragment.TYPE_YOUTUBE;
    }
}
