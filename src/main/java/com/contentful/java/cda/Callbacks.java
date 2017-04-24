package com.contentful.java.cda;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;

final class Callbacks {
  private Callbacks() {
    throw new AssertionError();
  }

  static <O extends CDAResource, C extends CDAResource> CDACallback<C> subscribeAsync(
      Observable<O> observable, CDACallback<C> callback, CDAClient client) {
    ConnectableObservable<O> connectable = observable.observeOn(Schedulers.io()).publish();

    callback.setDisposable(connectable.subscribe(
        new SuccessAction<O>(callback, client),
        new FailureAction(callback, client)));
    
    connectable.connect();
    return callback;
  }

  static abstract class BaseAction<E> implements Consumer<E> {
    protected final CDACallback<? extends CDAResource> callback;

    protected final CDAClient client;

    BaseAction(CDACallback<? extends CDAResource> callback, CDAClient client) {
      this.callback = callback;
      this.client = client;
    }

    @Override public void accept(E e) {
      if (!callback.isCancelled()) {
        doCall(e);
      }
      callback.unsubscribe();
    }

    protected abstract void doCall(E e);

    protected void execute(Runnable r) {
      client.callbackExecutor.execute(r);
    }
  }

  static class SuccessAction<E extends CDAResource> extends BaseAction<E> {
    SuccessAction(CDACallback<? extends CDAResource> callback, CDAClient client) {
      super(callback, client);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doCall(E e) {
      execute(new SuccessRunnable<E>(e, (CDACallback<E>) callback));
    }
  }

  static class FailureAction extends BaseAction<Throwable> {
    FailureAction(CDACallback<? extends CDAResource> callback, CDAClient client) {
      super(callback, client);
    }

    @Override protected void doCall(Throwable t) {
      execute(new FailureRunnable(t, callback));
    }
  }

  static class SuccessRunnable<E extends CDAResource> implements Runnable {
    private final E result;

    private final CDACallback<E> callback;

    SuccessRunnable(E result, CDACallback<E> callback) {
      this.result = result;
      this.callback = callback;
    }

    @Override public void run() {
      if (!callback.isCancelled()) {
        callback.onSuccess(result);
      }
    }
  }

  static class FailureRunnable implements Runnable {
    private final Throwable throwable;

    private final CDACallback<? extends CDAResource> callback;

    FailureRunnable(Throwable throwable, CDACallback<? extends CDAResource> callback) {
      this.throwable = throwable;
      this.callback = callback;
    }

    @Override public void run() {
      if (!callback.isCancelled()) {
        callback.onFailure(throwable);
      }
    }
  }
}
