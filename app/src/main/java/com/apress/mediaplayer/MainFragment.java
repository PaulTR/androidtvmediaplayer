package com.apress.mediaplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 9/16/15.
 */
public class MainFragment extends BrowseFragment implements OnItemViewClickedListener{

    private List<Video> mVideos = new ArrayList<Video>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadData();
        initUi();

        loadRows();

        setOnItemViewClickedListener( this );
    }

    private void loadData() {
        String json = Utils.loadJSONFromResource( getActivity(), R.raw.videos );
        Type collection = new TypeToken<ArrayList<Video>>(){}.getType();

        Gson gson = new Gson();
        mVideos = gson.fromJson( json, collection );
    }

    private void initUi() {
        setTitle( "Title" );
        setHeadersState(HEADERS_ENABLED);

        setHeadersTransitionOnBackEnabled(true);

        setSearchAffordanceColor(R.color.search_button_color);

        //Search Clicked event listener is required for the search orb to appear
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent( getActivity(), MediaSearchActivity.class );
                startActivity( intent );
            }
        });

    }

    private void loadRows() {
        ArrayObjectAdapter adapter = new ArrayObjectAdapter( new ListRowPresenter() );
        CardPresenter presenter = new CardPresenter();

        List<String> categories = getCategories();

        if( categories == null || categories.isEmpty() )
            return;

        for( String category : categories ) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter( presenter );
            for( Video movie : mVideos ) {
                if( category.equalsIgnoreCase( movie.getCategory() ) )
                    listRowAdapter.add( movie );
            }
            if( listRowAdapter.size() > 0 ) {
                HeaderItem header = new HeaderItem( adapter.size() - 1, category );
                adapter.add( new ListRow( header, listRowAdapter ) );
            }
        }

        setupPreferences(adapter);

        setAdapter(adapter);
    }

    private List<String> getCategories() {
        if( mVideos == null )
            return null;

        List<String> categories = new ArrayList<String>();
        for( Video movie : mVideos ) {
            if( !categories.contains( movie.getCategory() ) ) {
                categories.add( movie.getCategory() );
            }
        }

        return categories;
    }

    private void setupPreferences( ArrayObjectAdapter adapter ) {
        HeaderItem gridHeader = new HeaderItem( adapter.size(), "Preferences" );
        PreferenceCardPresenter presenter = new PreferenceCardPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter( presenter );
        gridRowAdapter.add( getResources().getString( R.string.preference_settings ) );
        adapter.add( new ListRow( gridHeader, gridRowAdapter ) );
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if( item instanceof Video ) {
            Video movie = (Video) item;
            Intent intent = new Intent( getActivity(), VideoDetailActivity.class );
            intent.putExtra( VideoDetailsFragment.EXTRA_VIDEO, movie );
            startActivity( intent );
        } else if( getString( R.string.preference_settings ).equals( item ) ) {
            Intent intent = new Intent( getActivity(), SettingsActivity.class );
            startActivity( intent );
        }
    }

}
