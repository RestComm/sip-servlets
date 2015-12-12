/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.servlet.sip.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread factory which names threads by "pool-<basename>-thread-n".
 * This is a replacement for Executors.defaultThreadFactory() to be able to identify pools.
 * Optionally a delegate thread factory can be given which creates the Thread
 * object itself, if no delegate has been given, Executors.defaultThreadFactory is used.
 * @author Alerant
 *
 */
public class NamingThreadFactory implements ThreadFactory {
    private ThreadFactory delegate;
    private String baseName;
    private AtomicInteger index;

    public NamingThreadFactory(String baseName) {
        this(baseName, null);
    }

    public NamingThreadFactory(String baseName, ThreadFactory delegate) {
        this.baseName = baseName;
        this.delegate = delegate;
        if (this.delegate == null) {
            this.delegate = Executors.defaultThreadFactory();
        }
        this.index = new AtomicInteger(1);
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = "pool-" + baseName + "-thread-" + index.getAndIncrement();
        Thread ret = delegate.newThread(r);
        ret.setName(name);
        return ret;
    }
}
