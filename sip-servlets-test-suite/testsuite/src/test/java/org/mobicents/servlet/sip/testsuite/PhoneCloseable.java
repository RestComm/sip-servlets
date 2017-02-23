package org.mobicents.servlet.sip.testsuite;

import java.io.Closeable;
import java.io.IOException;
import org.cafesip.sipunit.SipPhone;

public class PhoneCloseable implements Closeable {
    SipPhone phone;

    public PhoneCloseable(SipPhone phone) {
        this.phone = phone;
    }

    public void close() throws IOException {
        phone.dispose();
    }    
}
