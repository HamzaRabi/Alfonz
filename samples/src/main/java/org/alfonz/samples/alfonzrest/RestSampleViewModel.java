package org.alfonz.samples.alfonzrest;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import org.alfonz.rest.HttpException;
import org.alfonz.rest.call.CallManager;
import org.alfonz.rest.call.Callback;
import org.alfonz.samples.alfonzarch.BaseViewModel;
import org.alfonz.samples.alfonzrest.entity.RepoEntity;
import org.alfonz.samples.alfonzrest.rest.RestHttpLogger;
import org.alfonz.samples.alfonzrest.rest.RestResponseHandler;
import org.alfonz.samples.alfonzrest.rest.provider.RepoServiceProvider;
import org.alfonz.utility.NetworkUtility;
import org.alfonz.view.StatefulLayout;

import retrofit2.Call;
import retrofit2.Response;


public class RestSampleViewModel extends BaseViewModel implements LifecycleObserver
{
	public final ObservableField<Integer> state = new ObservableField<>();
	public final ObservableField<RepoEntity> repo = new ObservableField<>();

	private CallManager mCallManager = new CallManager(new RestResponseHandler(), new RestHttpLogger());


	@OnLifecycleEvent(Lifecycle.Event.ON_START)
	public void onStart()
	{
		// load data
		if(repo.get() == null) loadData();
	}


	@Override
	public void onCleared()
	{
		super.onCleared();

		// cancel async tasks
		if(mCallManager != null) mCallManager.cancelRunningCalls();
	}


	public void refreshData()
	{
		loadData();
	}


	private void loadData()
	{
		if(NetworkUtility.isOnline(getApplicationContext()))
		{
			String callType = RepoServiceProvider.REPO_CALL_TYPE;
			if(!mCallManager.hasRunningCall(callType))
			{
				// show progress
				state.set(StatefulLayout.PROGRESS);

				// enqueue call
				Call<RepoEntity> call = RepoServiceProvider.getService().repo("petrnohejl", "Alfonz");
				RepoCallback callback = new RepoCallback(mCallManager);
				mCallManager.enqueueCall(call, callback, callType);
			}
		}
		else
		{
			// show offline
			state.set(StatefulLayout.OFFLINE);
		}
	}


	private void setState(ObservableField<RepoEntity> data)
	{
		if(data.get() != null)
		{
			state.set(StatefulLayout.CONTENT);
		}
		else
		{
			state.set(StatefulLayout.EMPTY);
		}
	}


	private class RepoCallback extends Callback<RepoEntity>
	{
		public RepoCallback(CallManager callManager)
		{
			super(callManager);
		}


		@Override
		public void onSuccess(@NonNull Call<RepoEntity> call, @NonNull Response<RepoEntity> response)
		{
			repo.set(response.body());
			setState(repo);
		}


		@Override
		public void onError(@NonNull Call<RepoEntity> call, @NonNull HttpException exception)
		{
			handleError(mCallManager.getHttpErrorMessage(exception));
			setState(repo);
		}


		@Override
		public void onFail(@NonNull Call<RepoEntity> call, @NonNull Throwable throwable)
		{
			handleError(mCallManager.getHttpErrorMessage(throwable));
			setState(repo);
		}
	}
}
