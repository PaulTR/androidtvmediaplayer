package com.apress.mediaplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 9/16/15.
 */
public class VideoDetailsFragment extends DetailsFragment implements OnItemViewClickedListener {

    public static final String EXTRA_VIDEO = "extra_video";
    public static final long ACTION_WATCH = 1;

    private Video mVideo;

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            row.setImageBitmap(getActivity(), bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    DetailsOverviewRow row;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVideo = (Video) getActivity().getIntent().getSerializableExtra( EXTRA_VIDEO );


        row = new DetailsOverviewRow( mVideo );

        //addAction has been deprecated
        row.setActionsAdapter(new SparseArrayObjectAdapter() {
            @Override
            public int size() {
                return 1;
            }

            @Override
            public Object get(int position) {
                if (position == 0)
                    return new Action(ACTION_WATCH, "Watch", "");

                else return null;
            }
        });

        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        FullWidthDetailsOverviewRowPresenter presenter =
                new FullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());

        // set detail background and style
        presenter.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.black));
        presenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_WATCH) {
                    //Toast.makeText(getActivity(), "Watch this thing", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent( getActivity(), PlayerActivity.class );
                    intent.putExtra( VideoDetailsFragment.EXTRA_VIDEO, mVideo );
                    startActivity( intent );
                }
            }
        });

        presenterSelector.addClassPresenter(DetailsOverviewRow.class, presenter);

        presenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());

        ArrayObjectAdapter adapter = new ArrayObjectAdapter( presenterSelector );
        adapter.add(row);

        loadRelatedMedia(adapter);
        setAdapter(adapter);

        Picasso.with(getActivity()).load(mVideo.getPoster()).resize(274, 274).into(target);


        setOnItemViewClickedListener( this );
    }

    private void loadRelatedMedia( ArrayObjectAdapter adapter ) {

        String json = Utils.loadJSONFromResource( getActivity(), R.raw.videos );
        Gson gson = new Gson();
        Type collection = new TypeToken<ArrayList<Video>>(){}.getType();
        List<Video> videos = gson.fromJson( json, collection );
        if( videos == null )
            return;

        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter( new CardPresenter() );

        for( Video video : videos ) {
            if( video.getCategory().equals( mVideo.getCategory() ) && !video.getTitle().equals( mVideo.getTitle() ) ) {
                listRowAdapter.add( video );
            }
        }

        if( listRowAdapter.size() == 0 )
            return;

        HeaderItem header = new HeaderItem( 0, "Related" );
        adapter.add(new ListRow(header, listRowAdapter));
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
