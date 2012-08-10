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

/**
 * @author Emanuel Muckenhuber
 */
interface Constants {

    String ACCESS_LOG = "access-log";
    String ADDITIONAL_PARAMETERABLE_HEADERS = "additional-parameterable-heders";
    String ALIAS = "alias";
    String BASE_TIMER_INTERVAL = "base-timer-interval";
    String CA_CERTIFICATE_FILE = "ca-certificate-file";
    String CA_CERTIFICATE_PASSWORD = "ca-certificate-password";
    String CA_REVOCATION_URL = "ca-revocation-url";
    String CACHE_CONTAINER = "cache-container";
    String CACHE_NAME = "cache-name";
    String CERTIFICATE_FILE = "certificate-file";
    String CERTIFICATE_KEY_FILE = "certificate-key-file";
    String CHECK_INTERVAL = "check-interval";
    String CIPHER_SUITE = "cipher-suite";
    String CONDITION = "condition";
    String CONFIGURATION = "configuration";
    String CONNECTOR = "connector";
    String APPLICATION_ROUTER = "application-router";
    String CANCELED_TIMER_TASKS_PURGE_PERIOD = "canceled-timer-tasks-purge-period";
    String CONCURRENCY_CONTROL_MODE = "concurrency-control-mode";
    String CONGESTION_CONTROL_INTERVAL = "congestion-control-interval";
    String CONTAINER = "container";
    String CONTAINER_CONFIG = CONFIGURATION;
    String DEFAULT_VIRTUAL_SERVER = "default-virtual-server";
    String DEFAULT_WEB_MODULE = "default-web-module";
    String DEVELOPMENT = "development";
    String DIALOG_PENDING_REQUEST_CHECKING = "dialog-pending-request-checking";
    String DIRECTORY = "directory";
    String DISABLED = "disabled";
    String DISPLAY_SOURCE_FRAGMENT = "display-source-fragment";
    String DOMAIN = "domain";
    String DUMP_SMAP = "dump-smap";
    String ENABLED = "enabled";
    String ENABLE_LOOKUPS = "enable-lookups";
    String ENABLE_WELCOME_ROOT = "enable-welcome-root";
    String ERROR_ON_USE_BEAN_INVALID_CLASS_ATTRIBUTE = "error-on-use-bean-invalid-class-attribute";
    String EXECUTOR = "executor";
    String EXTENDED = "extended";
    String FILE_ENCODING = "file-encoding";
    String FLAGS = "flags";
    String GENERATE_STRINGS_AS_CHAR_ARRAYS = "generate-strings-as-char-arrays";
    String INSTANCE_ID = "instance-id";
    String JAVA_ENCODING = "java-encoding";
    String JSP_CONFIGURATION = "jsp-configuration";
    String KEEP_GENERATED = "keep-generated";
    String KEY_ALIAS = "key-alias";
    String KEYSTORE_TYPE = "keystore-type";
    String LISTINGS = "listings";
    String MAPPED_FILE = "mapped-file";
    String MAX_CONNECTIONS = "max-connections";
    String MAX_DEPTH = "max-depth";
    String MAX_POST_SIZE = "max-post-size";
    String MAX_SAVE_POST_SIZE = "max-save-post-size";
    String MIME_MAPPING = "mime-mapping";
    String MODIFICATION_TEST_INTERVAL = "modification-test-interval";
    String NAME = "name";
    String NATIVE = "native";
    String PASSWORD = "password";
    String PATH = "path";
    String PATTERN = "pattern";
    String PREFIX = "prefix";
    String PROTOCOL = "protocol";
    String PROXY_NAME = "proxy-name";
    String PROXY_PORT = "proxy-port";
    String REAUTHENTICATE = "reauthenticate";
    String READ_ONLY = "read-only";
    String RECOMPILE_ON_FAIL = "recompile-on-fail";
    String REDIRECT_PORT = "redirect-port";
    String RELATIVE_TO = "relative-to";
    String RESOLVE_HOSTS = "resolve-hosts";
    String REWRITE = "rewrite";
    String ROTATE = "rotate";
    String SCHEME = "scheme";
    String SCRATCH_DIR = "scratch-dir";
    String SECRET = "secret";
    String SECURE = "secure";
    String SENDFILE = "sendfile";
    String SESSION_CACHE_SIZE = "session-cache-size";
    String SESSION_TIMEOUT = "session-timeout";
    String SIP_APP_DISPATCHER_CLASS = "app-dispatcher-class";
    String SIP_PATH_NAME ="path-name";
    String SIP_STACK_PROPS = "stack-properties";
    String SMAP = "smap";
    String SOCKET_BINDING = "socket-binding";
    String SOURCE_VM = "source-vm";
    String SSL = "ssl";
    String SSO = "sso";
    String STATIC_RESOURCES = "static-resources";
    String STATIC_SERVER_ADDRESS = "static-server-address";
    String STATIC_SERVER_PORT = "static-server-port";
    String STUN_SERVER_ADDRESS = "stun-server-address";
    String STUN_SERVER_PORT = "stun-server-port";
    String SUBSTITUTION = "substitution";
    String SUBSYSTEM = "subsystem";
    String T2_INTERVAL = "t2-interval";
    String T4_INTERVAL = "t4-interval";
    String TAG_POOLING = "tag-pooling";
    String TARGET_VM = "target-vm";
    String TEST = "test";
    String TIMER_D_INTERVAL = "timer-d-interval";
    String TRIM_SPACES = "trim-spaces";
    String TRUSTSTORE_TYPE = "truststore-type";
    String USE_PRETTY_ENCODING = "use-pretty-encoding";
    String USE_STATIC_ADDRESS = "use-static-address";
    String USE_STUN = "use-stun";
    String VALUE = "value";
    String VERIFY_CLIENT = "verify-client";
    String VERIFY_DEPTH = "verify-depth";
    String VIRTUAL_SERVER = "virtual-server";
    String WEBDAV = "webdav";
    String WELCOME_FILE = "welcome-file";
    String X_POWERED_BY = "x-powered-by";

    /* Connect stats attributes */
    String BYTES_SENT = "bytesSent";
    String BYTES_RECEIVED = "bytesReceived";
    String PROCESSING_TIME = "processingTime";
    String ERROR_COUNT = "errorCount";
    String MAX_TIME = "maxTime";
    String REQUEST_COUNT = "requestCount";

    String LOAD_TIME ="load-time";
    String MIN_TIME = "min-time";

}
