package uk.co.jbothma.protege.protplug;

import java.util.EventListener;

import uk.co.jbothma.protege.protplug.SubclassEvent;

public interface SubclassEventListener extends EventListener {
    public void myEventOccurred(SubclassEvent evt);
}
