package uk.co.jbothma.protege.protplug;

import java.util.EventListener;

public interface RelationEventListener extends EventListener {
    public void myEventOccurred(RelationEvent evt);
}