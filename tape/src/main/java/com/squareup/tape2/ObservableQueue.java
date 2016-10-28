package com.squareup.tape2;

import rx.Observable;
import rx.Scheduler;
import rx.Single;
import rx.exceptions.Exceptions;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 *
 * This class brings two changes over {@link ObjectQueue}.
 * <ul>
 * <li>
 *     Reactive APIs over {@link ObjectQueue}
 * </li>
 * <li>
 *     Every operation is synchronized and posted on queueScheduler thread.
 * </li>
 * </ul>
 *
 * <p> Closing ObjectQueue is asynchronous and is posted at the end of thread.</p>
 *
 * <p>If an operation is posted to queue after the close operation and before close returned, it will fail.</p>
 * @param <T>
 */
final class ObservableQueue<T> {

    private final ObjectQueue<T> objectQueue;

    private final Scheduler queueScheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    public static <T> ObservableQueue<T> createPersistedObservableQueue(
            final File file,
            final ObjectQueue.Converter<T> converter) throws IOException {
        return new ObservableQueue<>(file, converter);
    }

    public static <T> ObservableQueue<T> createInMemoryObservableQueue() {
        return new ObservableQueue<>();
    }

    private ObservableQueue(File file, ObjectQueue.Converter<T> converter) throws IOException {
        objectQueue = ObjectQueue.create(file, converter);
    }

    private ObservableQueue() {
        objectQueue = ObjectQueue.<T>createInMemory();
    }

    public Single<Boolean> close() {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    objectQueue.close();
                    return true;
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            }
        }).subscribeOn(queueScheduler);
    }

    public Single<Integer> size() {
        return Single.defer(new Func0<Single<Integer>>() {
            @Override
            public Single<Integer> call() {
                return Single.just(objectQueue.size());
            }
        }).subscribeOn(queueScheduler);
    }

    public Single<Boolean> add(final T entry) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    objectQueue.add(entry);
                    return true;
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            }
        }).subscribeOn(queueScheduler);
    }

    public Single<T> peek() {
        return Single.defer(new Func0<Single<T>>() {
            @Override
            public Single<T> call() {
                try {
                    return Single.just(objectQueue.peek());
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            }
        }).subscribeOn(queueScheduler);
    }

    public Single<List<T>> peek(final int n) {
        return Single.defer(new Func0<Single<List<T>>>() {
            @Override
            public Single<List<T>> call() {
                try {
                    return Single.just(objectQueue.peek(n));
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            }
        }).subscribeOn(queueScheduler);
    }

    public Single<Boolean> remove(final int n) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    objectQueue.remove(n);
                    return true;
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            }
        }).subscribeOn(queueScheduler);
    }

    public Single<Boolean> remove() {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    objectQueue.remove();
                    return true;
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            }
        }).subscribeOn(queueScheduler);
    }
}
