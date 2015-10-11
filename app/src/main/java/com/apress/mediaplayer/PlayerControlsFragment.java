package com.apress.mediaplayer;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v17.leanback.app.PlaybackOverlayFragment;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Paul on 10/10/15.
 */
public class PlayerControlsFragment extends PlaybackOverlayFragment {

    public interface PlayerControlsListener {
        void play();
        void pause();
    }

    private PlayerControlsListener mControlsCallback;

    private Video mSelectedVideo;

    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private ArrayObjectAdapter mSecondaryActionsAdapter;

    private PlaybackControlsRow mPlaybackControlsRow;

    private PlaybackControlsRow.PlayPauseAction mPlayPauseAction;

    private PlaybackControlsRow.RepeatAction mRepeatAction;
    private PlaybackControlsRow.ShuffleAction mShuffleAction;
    private PlaybackControlsRow.FastForwardAction mFastForwardAction;
    private PlaybackControlsRow.RewindAction mRewindAction;
    private PlaybackControlsRow.SkipNextAction mSkipNextAction;
    private PlaybackControlsRow.SkipPreviousAction mSkipPreviousAction;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackgroundType(PlaybackOverlayFragment.BG_LIGHT);
        setFadingEnabled(false);

        mControlsCallback = (PlayerControlsListener) getActivity();
        mSelectedVideo = (Video) getActivity().getIntent().getSerializableExtra(VideoDetailsFragment.EXTRA_VIDEO);

        ClassPresenterSelector ps = new ClassPresenterSelector();
        //Either include a presenter to show content on the overlay, or leave it blank to show just the controls
        PlaybackControlsRowPresenter playbackControlsRowPresenter = new PlaybackControlsRowPresenter( new DescriptionPresenter() );

        playbackControlsRowPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            public void onActionClicked(Action action) {
                Log.e("Video", "Action clicked: " + action.getLabel1());
                if(action.getId() == mPlayPauseAction.getId()) {
                    if (mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PLAY) {
                        setFadingEnabled(true);
                        mControlsCallback.play();
                        mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
                    } else {
                        setFadingEnabled( false );
                        mControlsCallback.pause();
                    }
                } else if( action.getId() == mRewindAction.getId() ) {
                    Toast.makeText( getActivity(), "Rewind", Toast.LENGTH_SHORT ).show();
                }
                if (action instanceof PlaybackControlsRow.MultiAction) {
                    ((PlaybackControlsRow.MultiAction) action).nextIndex();
                    notifyChanged(action);
                }
            }
        });

        playbackControlsRowPresenter.setSecondaryActionsHidden(false);

        ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        ps.addClassPresenter(ListRow.class, new ListRowPresenter());
        mRowsAdapter = new ArrayObjectAdapter(ps);

        //addplaybackcontrolsrow
        mPlaybackControlsRow = new PlaybackControlsRow( mSelectedVideo );
        mRowsAdapter.add(mPlaybackControlsRow);

        //addplaybackcontrolsrow
        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mSecondaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);
        mPlaybackControlsRow.setSecondaryActionsAdapter(mSecondaryActionsAdapter);

        //create actions
        mPlayPauseAction = new PlaybackControlsRow.PlayPauseAction(getActivity());
        mRepeatAction = new PlaybackControlsRow.RepeatAction(getActivity());
        mShuffleAction = new PlaybackControlsRow.ShuffleAction(getActivity());
        mSkipNextAction = new PlaybackControlsRow.SkipNextAction(getActivity());
        mSkipPreviousAction = new PlaybackControlsRow.SkipPreviousAction(getActivity());
        mFastForwardAction = new PlaybackControlsRow.FastForwardAction(getActivity());
        mRewindAction = new PlaybackControlsRow.RewindAction(getActivity());

        mPrimaryActionsAdapter.add(mSkipPreviousAction);
        mPrimaryActionsAdapter.add(mRewindAction);
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        mPrimaryActionsAdapter.add(mFastForwardAction);
        mPrimaryActionsAdapter.add(mSkipNextAction);

        mSecondaryActionsAdapter.add(mRepeatAction);
        mSecondaryActionsAdapter.add(mShuffleAction);
        mSecondaryActionsAdapter.add(new PlaybackControlsRow.HighQualityAction(getActivity()));
        mSecondaryActionsAdapter.add(new PlaybackControlsRow.ClosedCaptioningAction(getActivity()));

        setAdapter(mRowsAdapter);

    }

    private void notifyChanged(Action action) {
        ArrayObjectAdapter adapter = mPrimaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
            return;
        }
        adapter = mSecondaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
            return;
        }
    }

    static class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {
        @Override
        protected void onBindDescription(ViewHolder viewHolder, Object item) {
            viewHolder.getTitle().setText(((Video) item).getTitle());
        }
    }
}
