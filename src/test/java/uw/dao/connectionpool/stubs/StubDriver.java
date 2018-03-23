/*
 * Copyright (C) 2014 Brett Wooldridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uw.dao.connectionpool.stubs;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Brett Wooldridge
 */
public class StubDriver implements Driver {
    private static final Driver driver;
    private static long connectionDelay;

    static {
        driver = new StubDriver();
        try {
            DriverManager.registerDriver(driver);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setConnectDelayMs(final long delay) {
        connectionDelay = delay; //MILLISECONDS.toNanos(delay);
    }

    /**
     * {@inheritDoc}
     */
    public Connection connect(String url, Properties info) throws SQLException {
        if (connectionDelay > 0) {
//         final long start = nanoTime();
//         do {
//            // spin
//         } while (nanoTime() - start < connectionDelayNs);
//         UtilityElf.quietlySleep(connectionDelay);
        }

        return new StubConnection();
    }

    /**
     * {@inheritDoc}
     */
    public boolean acceptsURL(String url) throws SQLException {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public int getMajorVersion() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public int getMinorVersion() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean jdbcCompliant() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
