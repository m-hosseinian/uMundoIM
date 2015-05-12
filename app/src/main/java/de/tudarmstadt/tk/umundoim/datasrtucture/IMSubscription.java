package de.tudarmstadt.tk.umundoim.datasrtucture;

import org.umundo.core.Publisher;
import org.umundo.core.Subscriber;

/**
 * Created by Mohammad on 5/12/2015.
 */
public class IMSubscription {
    private Publisher publisher;
    private Subscriber subscriber;

    public IMSubscription(Publisher publisher, Subscriber subscriber) {
        this.publisher = publisher;
        this.subscriber = subscriber;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }
}
