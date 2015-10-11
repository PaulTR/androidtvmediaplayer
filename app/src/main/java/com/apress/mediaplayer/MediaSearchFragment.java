package com.apress.mediaplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.SearchFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SpeechRecognitionCallback;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 9/28/15.
 */
public class MediaSearchFragment extends SearchFragment implements SpeechRecognitionCallback, SearchFragment.SearchResultProvider, OnItemViewClickedListener {

    private ArrayObjectAdapter mRowsAdapter;
    private List<Video> mVideos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadData();
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setSearchResultProvider(this);
        setSpeechRecognitionCallback( this );
        setOnItemViewClickedListener( this );
    }

    private void loadData() {
        String json = Utils.loadJSONFromResource( getActivity(), R.raw.videos );
        Type collection = new TypeToken<ArrayList<Video>>(){}.getType();

        Gson gson = new Gson();
        mVideos = gson.fromJson( json, collection );
    }

    private void loadQuery( String query ) {
        if( query == null || query.length() == 0 )
            return;

        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        for( Video video : mVideos ) {
            if( video.getTitle() != null && video.getTitle().toLowerCase().contains( query.toLowerCase() ) ) {
                listRowAdapter.add( video );
            }
        }

        if(listRowAdapter.size() == 0 )
            return;

        HeaderItem header = new HeaderItem( "Results" );
        mRowsAdapter.add(new ListRow(header, listRowAdapter));


    }

    @Override
    public void recognizeSpeech() {
        Log.e( "TV", "recognizeSpeech" );
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return mRowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        Log.e( "TV", "onQueryTextChange: " + newQuery );
        if( mRowsAdapter != null )
            mRowsAdapter.clear();

        loadQuery( newQuery );
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.e( "TV", "onQueryTextSubmit: " + query );
        loadQuery( query );
        return false;
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if( item instanceof Video ) {
            Video video = (Video) item;
            Intent intent = new Intent( getActivity(), VideoDetailActivity.class );
            intent.putExtra( VideoDetailsFragment.EXTRA_VIDEO, video );
            startActivity( intent );
        }
    }
}
