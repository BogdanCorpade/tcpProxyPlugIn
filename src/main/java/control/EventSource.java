package main.java.control;

import java.util.Observable;

/**
 * Created by bcorpade on 4/7/2014.
 */
public class EventSource extends Observable {

    public void publishEvent(LogEvent logEvent) {
        setChanged();
        notifyObservers(logEvent);
    }
}




