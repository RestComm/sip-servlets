package org.mobicents.servlet.sip.testsuite;

import org.mobicents.servlet.sip.testsuite.excluded.*;

public class Unreferenced {
    private Absent absent;

    public Unreferenced() {
        absent = new Absent() {
			public void disappear() {
				System.out.println("gone!");
			}
		};
    }
}
