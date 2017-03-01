package io.oddworks.oddsample;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedHashSet;

import io.oddworks.device.authentication.OddAuthenticator;
import io.oddworks.device.exception.BadResponseCodeException;
import io.oddworks.device.metric.OddAppInitMetric;
import io.oddworks.device.metric.OddViewLoadMetric;
import io.oddworks.device.model.OddCollection;
import io.oddworks.device.model.OddConfig;
import io.oddworks.device.model.OddError;
import io.oddworks.device.model.OddProgress;
import io.oddworks.device.model.OddPromotion;
import io.oddworks.device.model.OddVideo;
import io.oddworks.device.model.OddView;
import io.oddworks.device.model.common.OddIdentifier;
import io.oddworks.device.model.common.OddResource;
import io.oddworks.device.model.common.OddResourceType;
import io.oddworks.device.request.OddCallback;
import io.oddworks.device.request.OddRequest;
import io.oddworks.device.request.RxOddCall;
import io.oddworks.device.service.OddRxBus;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_GET_ACCOUNTS = 0;

    private Context ctx = this;
    private AccountManager accountManager = null;
    private Account account = null;
    private String splashId = null;
    private String homepageId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);

        showSpinner();
        hideButton();
        getConfig();
    }

    /**
     * example using plain OddCallback
     */
    private void getConfig() {
        OddCallback<OddConfig> configCallback = new OddCallback<OddConfig>() {
            @Override
            public void onSuccess(OddConfig config) {
                homepageId = config.getViews().get("homepage");
                splashId = config.getViews().get("splash");

                OddAppInitMetric initMetric = new OddAppInitMetric();

                OddRxBus.publish(initMetric);

                Log.d(TAG, "getConfig success: " + config);

                if (config.getFeatures().getAuthentication().getEnabled()) {
                    Log.d(TAG, "authentication enabled");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkAuthentication();
                    }
                });
            }

            @Override
            public void onFailure(@NotNull Throwable exception) {
                handleRequestException("getConfig", exception);
            }
        };

        new OddRequest.Builder(ctx, OddResourceType.CONFIG)
                .account(account)
                .build()
                .enqueueRequest(configCallback);
    }

    /**
     * example login using Oddworks and RxOddCall
     */
    private void checkAuthentication() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS}, PERMISSIONS_GET_ACCOUNTS);
        } else {
            Account[] accounts = accountManager.getAccountsByType(getString(R.string.oddworks_account_type));
            if (accounts.length == 0) {
                hideSpinner();
                showButton();
                getView(splashId);
            } else {
                hideSpinner();
                hideButton();
                account = accounts[0];
                updateSignInMessage("Signed in as " + account.name);
                getView(homepageId);
            }
        }
    }

    /**
     * example using RxOddCall, wrapping OddRequest in an RxJava Observable
     */
    private void getView(final String viewId) {
        RxOddCall
                .observableFrom(new Action1<OddCallback<OddView>>() {
                    @Override
                    public void call(OddCallback<OddView> oddCallback) {
                        new OddRequest.Builder(ctx, OddResourceType.VIEW)
                                .account(account)
                                .resourceId(viewId)
                                .include("featuredCollections,promotion")
                                .build()
                                .enqueueRequest(oddCallback);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<OddView>() {
                    @Override
                    public void call(OddView oddView) {
                        LinkedHashSet<OddResource> featured = oddView.getIncludedByRelationship("featuredCollections");
                        LinkedHashSet<OddResource> promotions = oddView.getIncludedByRelationship("promotion");

                        OddPromotion promotion = (OddPromotion) promotions.iterator().next();
                        OddCollection collection1 = (OddCollection) featured.iterator().next();


                        OddViewLoadMetric viewLoadMetric = new OddViewLoadMetric("view", oddView.getId(), oddView.getTitle(), null);
                        OddRxBus.publish(viewLoadMetric);

                        Log.d(TAG, "getView success: promotion " + promotion.getTitle() + " collection: " + collection1.getTitle());
                        getVideos();
                        getCollectionEntities(collection1.getId());
                        addToWatchlist(collection1);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        handleRequestException("getView", throwable);
                    }
                });
    }

    private void getVideos() {
        RxOddCall
                .observableFrom(new Action1<OddCallback<LinkedHashSet<OddVideo>>>() {

                    @Override
                    public void call(OddCallback<LinkedHashSet<OddVideo>> oddCallback) {
                        new OddRequest.Builder(ctx, OddResourceType.VIDEO)
                                .account(account)
                                .build()
                                .enqueueRequest(oddCallback);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LinkedHashSet<OddVideo>>() {
                    @Override
                    public void call(LinkedHashSet<OddVideo> videos) {
                        Log.d(TAG, "getVideos success: " + videos.size());
                        Iterator<OddVideo> oddVideoIterator = videos.iterator();
                        if (oddVideoIterator.hasNext()) {
                            OddVideo vid = oddVideoIterator.next();
                            if (account != null) {
                                addToWatchlist(vid);
                                recordProgress(vid);
                                removeFromWatchlist(vid);
                            }
                        }
                    }
                }, new Action1<Throwable>() {

                    @Override
                    public void call(Throwable throwable) {
                        handleRequestException("getVideos", throwable);
                    }
                });
    }

    private void getCollectionEntities(final String collectionId) {
        RxOddCall
                .observableFrom(new Action1<OddCallback<LinkedHashSet<OddIdentifier>>>() {
                    @Override
                    public void call(OddCallback<LinkedHashSet<OddIdentifier>> oddCallback) {
                        new OddRequest.Builder(ctx, OddResourceType.COLLECTION)
                                .account(account)
                                .resourceId(collectionId)
                                .relationshipName(OddCollection.RELATIONSHIPS.ENTITIES)
                                .build()
                                .enqueueRequest(oddCallback);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LinkedHashSet<OddIdentifier>>() {
                    @Override
                    public void call(LinkedHashSet<OddIdentifier> identifiers) {
                        Log.d(TAG, "getCollectionEntities success: " + identifiers.size());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        handleRequestException("getCollectionEntities", throwable);
                    }
                });
    }

    private void addToWatchlist(final OddResource resource) {
        if (account == null) {
            Log.d(TAG, "addToWatchlist not called - no account");
        }
        RxOddCall
                .observableFrom(new Action1<OddCallback<OddResource>>() {
                    @Override
                    public void call(OddCallback<OddResource> oddResourceOddCallback) {
                        new OddRequest.Builder(ctx, OddResourceType.WATCHLIST)
                                .account(account)
                                .addResourceToWatchlist(account.name, resource)
                                .build()
                                .enqueueRequest(oddResourceOddCallback);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<OddResource>() {
                    @Override
                    public void call(OddResource oddResource) {
                        Log.d(TAG, "addToWatchlist success: " + oddResource.getId());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        handleRequestException("addToWatchlist", throwable);
                    }
                });
    }

    private void recordProgress(final OddVideo resource) {
        if (account == null) {
            Log.d(TAG, "recordProgress not called - no account");
        }
        RxOddCall
                .observableFrom(new Action1<OddCallback<OddProgress>>() {
                    @Override
                    public void call(OddCallback<OddProgress> oddCallback) {
                        new OddRequest.Builder(ctx, OddResourceType.PROGRESS)
                                .account(account)
                                .recordProgress(resource.getId(), resource.getDuration() - 204, false)
                                .build()
                                .enqueueRequest(oddCallback);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<OddProgress>() {
                    @Override
                    public void call(OddProgress oddResource) {
                        Log.d(TAG, "recordProgress success: " + oddResource.toJSONObject());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        handleRequestException("recordProgress", throwable);
                    }
                });
    }

    private void removeFromWatchlist(final OddResource resource) {
        if (account == null) {
            Log.d(TAG, "removeFromWatchlist not called - no account");
        }
        RxOddCall
                .observableFrom(new Action1<OddCallback<OddResource>>() {
                    @Override
                    public void call(OddCallback<OddResource> oddResourceOddCallback) {
                        new OddRequest.Builder(ctx, OddResourceType.WATCHLIST)
                                .account(account)
                                .removeResourceFromWatchlist(account.name, resource)
                                .build()
                                .enqueueRequest(oddResourceOddCallback);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<OddResource>() {
                    @Override
                    public void call(OddResource oddResource) {
                        Log.d(TAG, "removeFromWatchlist success: " + oddResource.getId());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        handleRequestException("removeFromWatchlist", throwable);
                    }
                });
    }

    private void handleRequestException(String requestType, Throwable throwable) {
        Log.w(TAG, requestType + " failed", throwable);
        if (throwable instanceof BadResponseCodeException) {
            int code = ((BadResponseCodeException) throwable).getCode();
            LinkedHashSet<OddError> errors = ((BadResponseCodeException) throwable).getOddErrors();
            Log.w(TAG, requestType + " code: " + code + " errors: " + errors);
        }
    }


    private void showButton() {
        View signInButton = findViewById(R.id.sign_in_button);
        if (signInButton != null) {
            signInButton.setVisibility(View.VISIBLE);
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addNewAccount(getString(R.string.oddworks_account_type), OddAuthenticator.AUTH_TOKEN_TYPE_ODDWORKS_DEVICE);
                }
            });
        }
    }

    private void hideButton() {
        View signInButton = findViewById(R.id.sign_in_button);
        if (signInButton != null) {
            signInButton.setVisibility(View.GONE);
        }
    }

    private void showSpinner() {
        View progressBar = findViewById(R.id.sign_in_spinner);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideSpinner() {
        View progressBar = findViewById(R.id.sign_in_spinner);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void updateSignInMessage(String message) {
        TextView signInMessage = (TextView) findViewById(R.id.sign_in_message);
        if (signInMessage != null) {
            signInMessage.setText(message);
        }
    }

    private void addNewAccount(String accountType, String authTokenType) {
        accountManager.addAccount(accountType, authTokenType, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
                try {
                    Bundle res = accountManagerFuture.getResult();
                    Toast.makeText(ctx, "Account was created", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "addNewAccount Bundle is " + res);
                    restartActivity();
                } catch (Exception e) {
                    Log.w(TAG, "addNewAccount failed", e);
                    Toast.makeText(ctx, "Account creation failed", Toast.LENGTH_SHORT).show();
                }
            }
        }, null);
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_GET_ACCOUNTS: {
                restartActivity();
            }
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}
