package com.squareup.tape2;

import com.squareup.burst.BurstJUnit4;
import com.squareup.burst.annotation.Burst;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(BurstJUnit4.class)
public class ObservableQueueTest {
    public enum QueueFactory {
        FILE() {
            @Override public <T> ObservableQueue<T> create(File file, FileObjectQueue.Converter<T> converter)
                    throws IOException {
                return ObservableQueue.createPersistedObservableQueue(file, converter);
            }
        },
        MEMORY() {
            @Override
            public <T> ObservableQueue<T> create(File file, FileObjectQueue.Converter<T> converter) {
                return ObservableQueue.createInMemoryObservableQueue();
            }
        };

        public abstract <T> ObservableQueue<T> create(File file, FileObjectQueue.Converter<T> converter)
                throws IOException;
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Burst
    QueueFactory factory;
    ObservableQueue<String> queue;

    @Before
    public void setUp() throws IOException {
        File parent = folder.getRoot();
        File file = new File(parent, "object-queue");

        queue = factory.create(file, new StringConverter());
        queue.add("one").toBlocking().value();
        queue.add("two").toBlocking().value();
        queue.add("three").toBlocking().value();
    }

    @Test
    public void size() {
        assertThat(queue.size().toBlocking().value()).isEqualTo(3);
    }

    @Test public void peek() throws IOException {
        assertThat(queue.peek().toBlocking().value()).isEqualTo("one");
    }

    @Test public void peekMultiple() throws IOException {
        assertThat(queue.peek(2).toBlocking().value()).containsExactly("one", "two");
    }

    @Test public void peekMaxCanExceedQueueDepth() throws IOException {
        assertThat(queue.peek(6).toBlocking().value()).containsExactly("one", "two", "three");
    }

    static class StringConverter implements FileObjectQueue.Converter<String> {
        @Override public String from(byte[] bytes) throws IOException {
            return new String(bytes, "UTF-8");
        }

        @Override public void toStream(String s, OutputStream os) throws IOException {
            os.write(s.getBytes("UTF-8"));
        }
    }
}

