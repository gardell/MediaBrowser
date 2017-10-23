package org.gardell.mediabrowser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.google.android.agera.MutableRepository;
import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.rvadapter.RepositoryAdapter;

import org.gardell.mediabrowser.model.Program;

import java.io.IOException;
import java.net.URL;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static com.google.android.agera.Preconditions.checkNotNull;
import static com.google.android.agera.Repositories.mutableRepository;
import static com.google.android.agera.Repositories.repositoryWithInitialValue;
import static com.google.android.agera.Result.absent;
import static com.google.android.agera.Result.absentIfNull;
import static com.google.android.agera.Result.failure;
import static com.google.android.agera.Suppliers.functionAsSupplier;
import static com.google.android.agera.net.HttpFunctions.httpFunction;
import static com.google.android.agera.net.HttpRequests.httpGetRequest;
import static com.google.android.agera.rvadapter.RepositoryAdapter.repositoryAdapter;
import static com.google.android.agera.rvdatabinding.DataBindingRepositoryPresenters.dataBindingRepositoryPresenterOf;

public final class DetailsActivity extends AppCompatActivity {

    @NonNull
    public static Intent detailsActivityIntent(@NonNull final Context context, final int id) {
        return new Intent(context, DetailsActivity.class)
                .putExtra(INTENT_EXTRA_ID, id);
    }

    private static final String INTENT_EXTRA_ID = "id";
    private static final String SAVE_INSTANCE_STATE_PROGRAM = "program";

    private Repository<Result<Program>> repository;

    private RepositoryAdapter repositoryAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview);
        postponeEnterTransition();

        final int id = getIntent().getExtras().getInt(INTENT_EXTRA_ID, -1);
        if (id == -1) {
            finish();
        }
        final URL url = Config.getProgramUrl(id);

        final Result<Program> initialProgram = savedInstanceState != null
                ? absentIfNull(savedInstanceState.<Program>getParcelable(
                        SAVE_INSTANCE_STATE_PROGRAM))
                : absent();

        final UiErrorHandler<Program> uiErrorHandler = new UiErrorHandler<>(this, true);
        recyclerView = (RecyclerView) checkNotNull(findViewById(R.id.recycler_view));
        final MutableRepository<Integer> refreshCounterRepository = mutableRepository(0);
        final SwipeRefreshLayout swipeRefresh =
                (SwipeRefreshLayout) checkNotNull(findViewById(R.id.swipe_refresh));
        swipeRefresh.setOnRefreshListener(
                () -> refreshCounterRepository.accept(refreshCounterRepository.get() + 1));

        repository = repositoryWithInitialValue(initialProgram)
                        .observe(refreshCounterRepository)
                        .onUpdatesPerLoop()
                        .goTo(THREAD_POOL_EXECUTOR)
                        .attemptGetFrom(functionAsSupplier(
                                httpFunction(),
                                httpGetRequest(url.toString())
                                        .compile()))
                        .orEnd(input -> {
                            DetailsActivity.this.runOnUiThread(
                                    () -> swipeRefresh.setRefreshing(false));
                            DetailsActivity.this.startPostponedEnterTransition();
                            return uiErrorHandler.apply(input);
                        })
                        .attemptTransform(input -> {
                            DetailsActivity.this.runOnUiThread(
                                    () -> swipeRefresh.setRefreshing(false));
                            final int responseCode = input.getResponseCode();
                            return responseCode >= 200 && responseCode < 300
                                    ? input.getBodyString()
                                    : failure(new IOException("ResponseCode: " + responseCode));
                        })
                        .orEnd(input -> {
                            DetailsActivity.this.startPostponedEnterTransition();
                            return uiErrorHandler.apply(input);
                        })
                        .attemptTransform(Json::programFromJson)
                        .orEnd(input -> {
                            DetailsActivity.this.startPostponedEnterTransition();
                            return uiErrorHandler.apply(input);
                        })
                        .thenTransform(Result::success)
                        .compile();
        repositoryAdapter = repositoryAdapter()
                .add(repository,
                        dataBindingRepositoryPresenterOf(Program.class)
                                .layout(R.layout.details_program)
                                .itemId(org.gardell.mediabrowser.BR.program)
                                .stableIdForItem(input -> (long) input.getId())
                                .handler(BR.image_url_listener,
                                        new StartPostponedEnterTransitionRequestListener(this))
                                .forResult())
                .build();
        repositoryAdapter.setHasStableIds(true);
        recyclerView.setAdapter(repositoryAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        repositoryAdapter.startObserving();
    }

    @Override
    protected void onStop() {
        super.onStop();
        repositoryAdapter.stopObserving();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.setAdapter(null);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        repository.get().ifSucceededSendTo(value ->
                outState.putParcelable(SAVE_INSTANCE_STATE_PROGRAM, value));
    }
}
