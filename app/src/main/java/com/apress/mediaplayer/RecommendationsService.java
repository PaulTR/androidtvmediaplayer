package com.apress.mediaplayer;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 10/20/15.
 */
public class RecommendationsService extends IntentService {

    private int MAX_RECOMMENDATIONS = 3;

    private NotificationManager mNotificationManager;

    private List<Video> mVideos;

    public RecommendationsService() {
        super("RecommendationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        loadData();
        if( mNotificationManager == null ) {
            mNotificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }

        int numOfRecommendations = MAX_RECOMMENDATIONS;
        if( mVideos == null ) {
            return;
        } else if( mVideos.size() < MAX_RECOMMENDATIONS ){
            numOfRecommendations = mVideos.size();
        }

        RecommendationBuilder builder = new RecommendationBuilder()
                .setContext(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher);

        for(int i = 0; i < numOfRecommendations; i++ ) {
            Video video = mVideos.get( i );
            Bitmap bitmap = null;
            try {
                bitmap = Picasso.with(this).load(video.getPoster()).resize(313, 176).get();
            } catch( IOException e ) {

            }
            builder.setBackground( video.getPoster() )
                    .setId( i )
                    .setPriority(numOfRecommendations - i )
                    .setTitle(video.getTitle())
                    .setDescription( "This is a description" )
                    .setIntent(buildPendingIntent(video, i + 1));

            if( bitmap != null ) {
                builder.setBitmap( bitmap );
            }

            Notification notification = builder.build();
            mNotificationManager.notify(i + 1, notification);
        }
    }

    private PendingIntent buildPendingIntent(Video video, long id ) {
        Intent detailsIntent = new Intent(this, VideoDetailActivity.class);
        detailsIntent.putExtra(VideoDetailsFragment.EXTRA_VIDEO, video);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(VideoDetailActivity.class);
        stackBuilder.addNextIntent(detailsIntent);
        // Ensure a unique PendingIntents, otherwise all
        // recommendations end up with the same PendingIntent
        detailsIntent.setAction(Long.toString(id));

        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        return intent;
    }

    private void loadData() {
        String json = Utils.loadJSONFromResource( getApplicationContext(), R.raw.videos );
        Type collection = new TypeToken<ArrayList<Video>>(){}.getType();

        Gson gson = new Gson();
        mVideos = gson.fromJson( json, collection );
    }


}
