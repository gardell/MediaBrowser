package org.gardell.mediabrowser;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.google.android.agera.MutableRepository;
import com.google.android.agera.Repository;
import com.google.android.agera.Result;
import com.google.android.agera.rvadapter.RepositoryAdapter;

import org.gardell.mediabrowser.model.Program;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static android.support.v4.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
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
import static org.gardell.mediabrowser.Config.PROGRAMS_URL;
import static org.gardell.mediabrowser.DetailsActivity.detailsActivityIntent;

public final class MainActivity extends AppCompatActivity {

    private static final String SAVE_INSTANCE_STATE_PROGRAMS = "programs";

    private RepositoryAdapter repositoryAdapter;
    private RecyclerView recyclerView;
    private Repository<Result<List<Program>>> repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview);
        final UiErrorHandler<List<Program>> uiErrorHandler = new UiErrorHandler<>(this, true);
        recyclerView = (RecyclerView) checkNotNull(findViewById(R.id.recycler_view));
        final MutableRepository<Integer> refreshCounterRepository = mutableRepository(0);
        final SwipeRefreshLayout swipeRefresh =
                (SwipeRefreshLayout) checkNotNull(findViewById(R.id.swipe_refresh));
        swipeRefresh.setOnRefreshListener(() ->
                refreshCounterRepository.accept(refreshCounterRepository.get() + 1));

        final Result<List<Program>> initialPrograms = savedInstanceState != null
                ? absentIfNull(savedInstanceState.getParcelableArrayList(
                        SAVE_INSTANCE_STATE_PROGRAMS))
                : absent();

        repository = repositoryWithInitialValue(initialPrograms)
                .observe(refreshCounterRepository)
                .onUpdatesPerLoop()
                .goTo(THREAD_POOL_EXECUTOR)
                .attemptGetFrom(functionAsSupplier(
                        httpFunction(),
                        httpGetRequest(PROGRAMS_URL.toString())
                                .compile()))
                .orEnd(input -> {
                    MainActivity.this.runOnUiThread(() -> swipeRefresh.setRefreshing(false));
                    return uiErrorHandler.apply(input);
                })
                .attemptTransform(input -> {
                    MainActivity.this.runOnUiThread(() -> swipeRefresh.setRefreshing(false));
                    final int responseCode = input.getResponseCode();
                    return responseCode >= 200 && responseCode < 300
                            ? input.getBodyString()
                            : failure(new IOException("ResponseCode: " + responseCode));
                })
                .orEnd(uiErrorHandler)
                .attemptTransform(Json::programsFromJson)
                .orEnd(uiErrorHandler)
                .thenTransform(Result::success)
                .compile();
        repositoryAdapter = repositoryAdapter()
                .add(repository,
                        dataBindingRepositoryPresenterOf(Program.class)
                                .layout(R.layout.listed_program)
                                .itemId(org.gardell.mediabrowser.BR.program)
                                .stableIdForItem(input -> (long) input.getId())
                                .handler(BR.on_click, (ClickListener<Program>) (value, view) ->
                                        startActivity(
                                                detailsActivityIntent(
                                                        MainActivity.this,
                                                        value.getId()),
                                                makeSceneTransitionAnimation(
                                                        MainActivity.this,
                                                        view.findViewById(R.id.image),
                                                        Integer.toString(value.getId()))
                                                        .toBundle()))
                                .forResultList())
                .build();
        repositoryAdapter.setHasStableIds(true);
        recyclerView.setAdapter(repositoryAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        repositoryAdapter.startObserving();
        repositoryAdapter.update();
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
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        repository.get().ifSucceededSendTo(value -> outState.putParcelableArrayList(
                SAVE_INSTANCE_STATE_PROGRAMS, new ArrayList<>(value)));
    }
}
