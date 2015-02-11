package com.example.viktoria.githubreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;


public class MainActivity extends Activity implements SearchFragment.OnSearchClickListener, UserProfileFragment.OnShareFbClickListener {
    public static final String TAG = "GITHUB_READER";
    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SearchFragment searchFragment = new SearchFragment();
        getFragmentManager().beginTransaction().replace(R.id.content_frame, searchFragment,
                "search_fr").commit();

    }

    @Override
    public void onSearchClicked(String username) {
        UserProfileFragment userProfileFragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString("username", username);
        userProfileFragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, userProfileFragment,
                "user_profile_fr").addToBackStack(
                "user_profile_fr").commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
//                boolean didCancel = FacebookDialog.getNativeDialogDidComplete(data);
//                String completionGesture = FacebookDialog.getNativeDialogCompletionGesture(data);
//                String postId = FacebookDialog.getNativeDialogPostId(data);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
        uiHelper.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
        uiHelper.onPause();
    }

    @Override
    public void onShareClicked(User u) {
        if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
            // Publish the post using the Share Dialog

            FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
                    .setLink(u.getHtml_url())
                    .build();
            uiHelper.trackPendingDialogCall(shareDialog.present());

        } else {
            // Fallback. For example, publish the post using the Feed Dialog
            publishFeedDialog(u);
        }

    }

    private void publishFeedDialog(User u) {
        Bundle params = new Bundle();

        params.putString("link", u.getHtml_url());

        WebDialog feedDialog = (
                new WebDialog.FeedDialogBuilder(this,
                        Session.getActiveSession(),
                        params))
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                                           FacebookException error) {
                        if (error == null) {
                            // When the story is posted, echo the success
                            // and the post Id.
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                Toast.makeText(MainActivity.this,
                                        "Posted story, id: " + postId,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // User clicked the Cancel button
                                Toast.makeText(getApplicationContext(),
                                        "Publish cancelled",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            // User clicked the "x" button
                            Toast.makeText(getApplicationContext(),
                                    "Publish cancelled",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Generic, ex: network error
                            Toast.makeText(getApplicationContext(),
                                    "Error posting story",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                })
                .build();
        feedDialog.show();
    }
}
