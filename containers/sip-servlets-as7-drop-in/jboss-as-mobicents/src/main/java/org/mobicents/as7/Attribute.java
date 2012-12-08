/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.as7;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emanuel Muckenhuber
 * @author Jean-Frederic Clere
 */
enum Attribute {
    UNKNOWN(null),

    APPLICATION_ROUTER(Constants.APPLICATION_ROUTER),
    ADDITIONAL_PARAMETERABLE_HEADERS(Constants.ADDITIONAL_PARAMETERABLE_HEADERS),
    BACK_TO_NORMAL_MEMORY_THRESHOLD(Constants.BACK_TO_NORMAL_MEMORY_THRESHOLD),
    BASE_TIMER_INTERVAL(Constants.BASE_TIMER_INTERVAL),
    CANCELED_TIMER_TASKS_PURGE_PERIOD(Constants.CANCELED_TIMER_TASKS_PURGE_PERIOD),
    CA_CERTIFICATE_FILE(Constants.CA_CERTIFICATE_FILE),
    CA_CERTIFICATE_PASSWORD(Constants.CA_CERTIFICATE_PASSWORD),
    CA_REVOCATION_URL(Constants.CA_REVOCATION_URL),
    CACHE_CONTAINER(Constants.CACHE_CONTAINER),
    CACHE_NAME(Constants.CACHE_NAME),
    CERTIFICATE_FILE(Constants.CERTIFICATE_FILE),
    CERTIFICATE_KEY_FILE(Constants.CERTIFICATE_KEY_FILE),
    CHECK_INTERVAL(Constants.CHECK_INTERVAL),
    CIPHER_SUITE(Constants.CIPHER_SUITE),
    CONGESTION_CONTROL_INTERVAL(Constants.CONGESTION_CONTROL_INTERVAL),
    CONGESTION_CONTROL_POLICY(Constants.CONGESTION_CONTROL_POLICY),
    CONCURRENCY_CONTROL_MODE(Constants.CONCURRENCY_CONTROL_MODE),
    DEFAULT_VIRTUAL_SERVER(Constants.DEFAULT_VIRTUAL_SERVER),
    DEFAULT_WEB_MODULE(Constants.DEFAULT_WEB_MODULE),
    DEVELOPMENT(Constants.DEVELOPMENT),
    DIALOG_PENDING_REQUEST_CHECKING(Constants.DIALOG_PENDING_REQUEST_CHECKING),
    DIRECTORY(Constants.DIRECTORY),
    DISABLED(Constants.DISABLED),
    DISPLAY_SOURCE_FRAGMENT(Constants.DISPLAY_SOURCE_FRAGMENT),
    DOMAIN(Constants.DOMAIN),
    DUMP_SMAP(Constants.DUMP_SMAP),
    ENABLED(Constants.ENABLED),
    ENABLE_WELCOME_ROOT(Constants.ENABLE_WELCOME_ROOT),
    ERROR_ON_USE_BEAN_INVALID_CLASS_ATTRIBUTE(Constants.ERROR_ON_USE_BEAN_INVALID_CLASS_ATTRIBUTE),
    EXECUTOR(Constants.EXECUTOR),
    EXTENDED(Constants.EXTENDED),
    FILE_ENCODING(Constants.FILE_ENCODING),
    FLAGS(Constants.FLAGS),
    GENERATE_STRINGS_AS_CHAR_ARRAYS(Constants.GENERATE_STRINGS_AS_CHAR_ARRAYS),
    INSTANCE_ID(Constants.INSTANCE_ID),
    JAVA_ENCODING(Constants.JAVA_ENCODING),
    KEEP_GENERATED(Constants.KEEP_GENERATED),
    KEY_ALIAS(Constants.KEY_ALIAS),
    KEYSTORE_TYPE(Constants.KEYSTORE_TYPE),
    LISTINGS(Constants.LISTINGS),
    MAPPED_FILE(Constants.MAPPED_FILE),
    MAX_CONNECTIONS(Constants.MAX_CONNECTIONS),
    MAX_DEPTH(Constants.MAX_DEPTH),
    MAX_POST_SIZE(Constants.MAX_POST_SIZE),
    MEMORY_THRESHOLD(Constants.MEMORY_THRESHOLD),
    MODIFICATION_TEST_INTERVAL(Constants.MODIFICATION_TEST_INTERVAL),
    MAX_SAVE_POST_SIZE(Constants.MAX_SAVE_POST_SIZE),
    NAME(Constants.NAME),
    NATIVE(Constants.NATIVE),
    OUTBOUND_PROXY(Constants.OUTBOUND_PROXY),
    PASSWORD(Constants.PASSWORD),
    PATH(Constants.PATH),
    PATTERN(Constants.PATTERN),
    PREFIX(Constants.PREFIX),
    PROTOCOL(Constants.PROTOCOL),
    PROXY_NAME(Constants.PROXY_NAME),
    PROXY_PORT(Constants.PROXY_PORT),
    READ_ONLY(Constants.READ_ONLY),
    REAUTHENTICATE(Constants.REAUTHENTICATE),
    REDIRECT_PORT(Constants.REDIRECT_PORT),
    RECOMPILE_ON_FAIL(Constants.RECOMPILE_ON_FAIL),
    RELATIVE_TO(Constants.RELATIVE_TO),
    RESOLVE_HOSTS(Constants.RESOLVE_HOSTS),
    ROTATE(Constants.ROTATE),
    SCHEME(Constants.SCHEME),
    SCRATCH_DIR(Constants.SCRATCH_DIR),
    SECRET(Constants.SECRET),
    SECURE(Constants.SECURE),
    SENDFILE(Constants.SENDFILE),
    SESSION_CACHE_SIZE(Constants.SESSION_CACHE_SIZE),
    SESSION_TIMEOUT(Constants.SESSION_TIMEOUT),
    SIP_APP_DISPATCHER_CLASS(Constants.SIP_APP_DISPATCHER_CLASS),
    SIP_PATH_NAME(Constants.SIP_PATH_NAME),
    SIP_STACK_PROPS(Constants.SIP_STACK_PROPS),
    SMAP(Constants.SMAP),
    SOCKET_BINDING(Constants.SOCKET_BINDING),
    SOURCE_VM(Constants.SOURCE_VM),
    STATIC_SERVER_ADDRESS(Constants.STATIC_SERVER_ADDRESS),
    STATIC_SERVER_PORT(Constants.STATIC_SERVER_PORT),
    STUN_SERVER_ADDRESS(Constants.STUN_SERVER_ADDRESS),
    STUN_SERVER_PORT(Constants.STUN_SERVER_PORT),
    SUBSTITUTION(Constants.SUBSTITUTION),
    T2_INTERVAL(Constants.T2_INTERVAL),
    T4_INTERVAL(Constants.T4_INTERVAL),    
    TARGET_VM(Constants.TARGET_VM),
    TIMER_D_INTERVAL(Constants.TIMER_D_INTERVAL),
    TRIM_SPACES(Constants.TRIM_SPACES),
    TRUSTSTORE_TYPE(Constants.TRUSTSTORE_TYPE),
    TAG_POOLING(Constants.TAG_POOLING),
    TEST(Constants.TEST),
    USE_PRETTY_ENCODING(Constants.USE_PRETTY_ENCODING),
    USE_STATIC_ADDRESS(Constants.USE_STATIC_ADDRESS),
    USE_STUN(Constants.USE_STUN),
    VERIFY_CLIENT(Constants.VERIFY_CLIENT),
    VERIFY_DEPTH(Constants.VERIFY_DEPTH),
    WEBDAV(Constants.WEBDAV),
    X_POWERED_BY(Constants.X_POWERED_BY),
    ENABLE_LOOKUPS(Constants.ENABLE_LOOKUPS),
    VALUE(Constants.VALUE),
    ;

    private final String name;

    Attribute(final String name) {
        this.name = name;
    }

    /**
     * Get the local name of this attribute.
     *
     * @return the local name
     */
    public String getLocalName() {
        return name;
    }

    private static final Map<String, Attribute> MAP;

    static {
        final Map<String, Attribute> map = new HashMap<String, Attribute>();
        for (Attribute element : values()) {
            final String name = element.getLocalName();
            if (name != null)
                map.put(name, element);
        }
        MAP = map;
    }

    public static Attribute forName(String localName) {
        final Attribute element = MAP.get(localName);
        return element == null ? UNKNOWN : element;
    }

    @Override
    public String toString() {
        return getLocalName();
    }
}
