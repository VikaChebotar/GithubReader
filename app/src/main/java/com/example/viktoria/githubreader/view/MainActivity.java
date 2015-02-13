package com.example.viktoria.githubreader.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.viktoria.githubreader.R;
import com.example.viktoria.githubreader.model.User;
import com.facebook.AppEventsLogger;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;

/**
 * Activity with two screens - SearchFragment and UserProfileFragment.
 * Implements SearchFragment.OnUserProfileOpenListener, UserProfileFragment.OnShareFbClickListener - callback interfaces to connect between fragments and activity.
 * Uses a UiLifecycleHelper to set a callback to handle the result of opening the FB Share Dialog.
 * If FB app is not installed in user phone, than need authorizationn and FeedDialog is shown.
 */
public class MainActivity extends Activity implements SearchFragment.OnUserProfileOpenListener, UserProfileFragment.OnShareFbClickListener {
    public static final String TAG = "GITHUB_READER"; //tag for all log messages in project
    private UiLifecycleHelper uiHelper;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String user_key = getString(R.string.user_intent_key);
        if (savedInstanceState != null && savedInstanceState.containsKey(user_key) && savedInstanceState.getParcelable(user_key) != null) {
            user = savedInstanceState.getParcelable(user_key);
        } else {
            //show SearchFragment
            FragmentManager fragmentManager = getFragmentManager();
            String tag = getString(R.string.search_fr_tag);
            SearchFragment searchFragment = (SearchFragment) fragmentManager.findFragmentByTag(tag);
            if (searchFragment == null) {
                searchFragment = new SearchFragment();
            }
            fragmentManager.beginTransaction().replace(R.id.content_frame, searchFragment,
                    tag).commit();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e(MainActivity.TAG, String.format(getString(R.string.error_log) + "%s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                String postId = FacebookDialog.getNativeDialogPostId(data);
                Log.d(MainActivity.TAG, getString(R.string.publish_log) + postId);
            }
        });
    }

    // need to handle FB loggin.
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private void onSessionStateChange(Session session, SessionState state,
                                      Exception exception) {
        if (state.isOpened()) {
            Log.d(MainActivity.TAG, getString(R.string.login_log));
            publishFeedDialog();

        }
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
        if (user != null) {
            outState.putParcelable(getString(R.string.user_intent_key), user);
        }
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

    //called when ShareBtn pressed in UserProfileFragment
    @Override
    public void onShareClicked() {
        if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
            // Publish the post using the Share Dialog
            FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
                    .setLink(user.getHtml_url())
                    .build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        } else {
            // Fallback. Publish the post using the Feed Dialog
            Session session = Session.getActiveSession();
            if (!session.isOpened() && !session.isClosed()) {
                session.openForRead(new Session.OpenRequest(this).setCallback(callback));
            } else {
                Session.openActiveSession(this, true, callback);
            }

        }

    }

    //Show FB FeedDialog to share link of user profile
    private void publishFeedDialog() {
        Bundle params = new Bundle();
        params.putString("link", user.getHtml_url());
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
                                        getString(R.string.publish_log) + postId,
                                        Toast.LENGTH_SHORT).show();
                                Log.d(MainActivity.TAG, getString(R.string.publish_log) + postId);
                            } else {
                                // User clicked the Cancel button
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.publish_canceled),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            // User clicked the "x" button
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.publish_canceled),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Generic, ex: network error
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.publish_error),
                                    Toast.LENGTH_SHORT).show();
                            Log.d(MainActivity.TAG, getString(R.string.publish_error));
                        }
                    }

                })
                .build();
        feedDialog.show();
    }


    private void showUserProfileFragment() {
        UserProfileFragment userProfileFragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.user_intent_key), user);
        userProfileFragment.setArguments(args);
        String tag = getString(R.string.user_profile_intent_key);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, userProfileFragment,
                tag).addToBackStack(
                tag).commit();
    }

//called when user entered username and press search btn
    @Override
    public void onUserProfileOpenClicked(User user) {
        this.user = user;
        showUserProfileFragment();
    }
}
