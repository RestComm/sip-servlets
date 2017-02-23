package org.mobicents.servlet.sip.testsuite;

import java.io.Closeable;
import java.io.IOException;
import org.cafesip.sipunit.SipStack;

public class StackCloseable implements Closeable {

    SipStack stack;

    public StackCloseable(SipStack stack) {
        this.stack = stack;
    }

    public void close() throws IOException {
        stack.dispose();
    }
}
