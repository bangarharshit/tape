Tape by Square, Inc.
====================

Tape is a collection of queue-related classes for Android and Java.

`QueueFile` is a lightning-fast, transactional, file-based FIFO. Addition and
removal from an instance is an O(1) operation and is atomic. Writes are
synchronous; data will be written to disk before an operation returns. The
underlying file is structured to survive process and even system crashes and if
an I/O exception is thrown during a mutating change, the change is aborted.

An `ObjectQueue` represents an ordering of arbitrary objects which can be backed
either by the filesystem (via `QueueFile`) or in memory only.

`TaskQueue` is a special object queue which holds `Task`s, objects which have a
notion of being executed. Instances are managed by an external executor which
prepares and executes enqueued tasks.

*Some examples are available on [the website][1].*

Changes over original:

1. ThreadSafe.
2. Reactive API.

*Example with observables*

```java
        File file = new File("Queuefile");
        ObjectQueue.Converter<String> queueObjectConverter = new ObjectQueue.Converter<String>() {
            @Override
            public String from(byte[] bytes) throws IOException {
                return new String(bytes);
            }

            @Override
            public void toStream(String o, OutputStream bytes) throws IOException {
                bytes.write(o.getBytes());
            }
        };

        ObservableQueue<String> objectObservableQueue = ObservableQueue.createPersistedObservableQueue(file, queueObjectConverter);

        objectObservableQueue
                .add("abc")
                .observeOn(Schedulers.immediate())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        // Persisted successful
                        System.out.println("aBoolean = [" + aBoolean + "]");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof IOException) {
                            // do something
                            System.out.println("throwable = [" + throwable + "]");
                        } else {
                            // do something else.
                        }
                    }
                });
        objectObservableQueue
                .size()
                .observeOn(Schedulers.immediate()).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {
                // Persisted successful
                System.out.println("integer = [" + integer + "]");
            }
        });
        objectObservableQueue.peek().observeOn(Schedulers.immediate()).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String string) {
                System.out.println("string = [" + string + "]");
            }
        });
        objectObservableQueue.close().observeOn(Schedulers.immediate()).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Boolean bBoolean) {
                System.out.println("bBoolean = [" + bBoolean + "]");
            }
        });
```        

Download
--------

Download [the latest JAR][2] or grab via Maven:
```xml
<dependency>
  <groupId>com.squareup</groupId>
  <artifactId>tape</artifactId>
  <version>1.2.3</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.squareup:tape:1.2.3'
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].



License
-------

    Copyright 2012 Square, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



 [1]: http://square.github.com/tape/
 [2]: https://search.maven.org/remote_content?g=com.squareup&a=tape&v=LATEST
 [snap]: https://oss.sonatype.org/content/repositories/snapshots/
