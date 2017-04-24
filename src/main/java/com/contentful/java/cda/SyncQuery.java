package com.contentful.java.cda;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import retrofit2.Response;

import static com.contentful.java.cda.Util.checkNotNull;

/** Represents a query to the Sync API. */
public class SyncQuery {
  final CDAClient client;

  final String syncToken;

  final SynchronizedSpace space;

  final boolean initial;

  private SyncQuery(Builder builder) {
    this.client = checkNotNull(builder.client, "Client must not be null.");
    this.syncToken = builder.syncToken;
    this.space = builder.space;
    this.initial = space == null && syncToken == null;
  }

  /**
   * Returns an {@link Observable} to which one can subscribe in order to fulfill this sync query.
   * @return {@link Observable} instance.
   */
  public Observable<SynchronizedSpace> observe() {
    final String token;
    if (space != null) {
      String nextSyncUrl = space.nextSyncUrl();
      if (nextSyncUrl == null) {
        throw new IllegalArgumentException("Provided space for synchronization is corrupt.");
      } else {
        token = Util.queryParam(space.nextSyncUrl(), "sync_token");
      }
    } else {
      token = syncToken;
    }
    return client.cacheAll(true)
        .flatMap(new Function<Cache, ObservableSource<Response<SynchronizedSpace>>>() {
          @Override public Observable<Response<SynchronizedSpace>> apply(Cache cache) {
            return client.service.sync(client.spaceId, initial ? initial : null, token);
          }
        }).map(new Function<Response<SynchronizedSpace>, SynchronizedSpace>() {
          @Override public SynchronizedSpace apply(Response<SynchronizedSpace> synchronizedSpace) {
            return ResourceFactory.sync(synchronizedSpace, space, client);
          }
        });
  }

  /**
   * Invokes the request to sync (blocking).
   * @return {@link SynchronizedSpace} instance.
   */
  public SynchronizedSpace fetch() {
    return observe().blockingFirst();
  }

  /**
   * Invokes the request to sync (asynchronously) with the provided {@code callback}.
   * @param callback callback.
   * @param <C> callback type.
   * @return the given callback instance.
   */
  @SuppressWarnings("unchecked")
  public <C extends CDACallback<SynchronizedSpace>> C fetch(C callback) {
    return (C) Callbacks.subscribeAsync(observe(), callback, client);
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {
    CDAClient client;

    String syncToken;

    SynchronizedSpace space;

    Builder setClient(CDAClient client) {
      this.client = client;
      return this;
    }

    Builder setSyncToken(String syncToken) {
      this.syncToken = syncToken;
      return this;
    }

    Builder setSpace(SynchronizedSpace space) {
      this.space = space;
      return this;
    }

    SyncQuery build() {
      return new SyncQuery(this);
    }
  }
}
