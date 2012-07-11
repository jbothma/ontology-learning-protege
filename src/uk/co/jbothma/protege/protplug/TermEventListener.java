package uk.co.jbothma.protege.protplug;

import java.util.EventListener;

public interface TermEventListener extends EventListener {
    public void myEventOccurred(TermEvent evt);
}