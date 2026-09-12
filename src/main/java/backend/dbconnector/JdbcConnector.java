package backend.dbconnector;/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import backend.Bracket;
import backend.Match;
import backend.Player;
import backend.Pool;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * PostgreSQL JDBC Connector<br>
 * This is generated code. The {@link JdbcConnector#connect()} method is implemented to connect to the
 * PostgreSQL Database using the appropriate JDBC Driver.
 *
 * <p><u>DRIVER LIBRARIES</u></p>
 * To run this class you will need to integrate the PostgreSQL JDBC Driver libraries in your project<br>
 * e.g. by using Maven Project Object Model (POM) <a href="https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.pom">postgresql-42.7.4.pom</a><br>
 * Additional features may require other libraries to be added to the runtime. Please read the PostgreSQL JDBC documentation for additional details.
 *
 * <p><u>JDBC URL</u></p>
 * The connection is using a JDBC URL of type "Database"<br>
 * URL pattern: "jdbc:postgresql://&lt;HOST&gt;:&lt;PORT&gt;/&lt;DATABASE&gt;"<br>
 * <ul>
 *   <li>HOST: the hostname or IP address of the machine where the database is running</li>
 *   <li>PORT: the TCP port number on which the PostgreSQL database listener is listening for incoming connections</li>
 *   <li>DATABASE: the name of the database to connect to</li>
 * </ul>
 *
 * <p><u>AUTHENTICATION</u></p>
 * The connection uses "User / Password" authentication
 * User and Password are passed as properties to the driver
 * <ul>
 *   <li>Property "user": the name of the user </li>
 *   <li>Property "password": the password for the account</li>
 * </ul>
 * The connection uses "User / Password" authentication
 */
public class JdbcConnector {
    private static final String PROP_USER = "user";
    private static final String PROP_PASSWORD = "password";
    private static final String JDBC_DB_PASSWORD = "JDBC_DB_PASSWORD";
    private Connection primeConnection;

    /**
     * Creates a jdbc connection to the POSTGRES database
     *
     * @return a new {@link Connection}
     * @throws Exception if something goes wrong
     */
    public Connection connect() throws Exception {
        Properties properties = new Properties();

        // JDBC URL
        // POSTGRES jdbc url: jdbc:postgresql://<HOST>:<PORT>/<DATABASE>
        String host = "localhost";
        String port = "5432";
        String database = "bracket_test2";
        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;

        // AUTHENTICATION
        String userName = "postgres";
        String password = "fcoder";
        properties.put(PROP_USER, userName);
        properties.put(PROP_PASSWORD, password);

        // PROPERTIES
        // custom properties (e.g. properties.put("charset", "UTF-8");)

        // DRIVER
        Class<? extends Driver> driverClass = Class.forName("org.postgresql.Driver").asSubclass(Driver.class);
        Driver driver = driverClass.getConstructor().newInstance();

        // CONNECTION
        return driver.connect(jdbcUrl, properties);
    }

    public JdbcConnector() {
        try {
            primeConnection = this.connect();
            primeConnection.isValid(10);
            System.out.println("INFO: Successfully connected and validated");
        }
        catch (Exception e) { System.out.println("ERROR: Failed to connect. Cause: " + e.getMessage()); }
    }



    public Connection getPrimeConnection() { return primeConnection; }

    /**
     * Load secret from system environment
     */
    private static String readSecret() {
        String secret = System.getenv(JDBC_DB_PASSWORD);
        if (secret == null) {
            String errorMessage = "Your connection is using password authentication," +
                    "you must set the value on the JDBC_DB_PASSWORD environment variable.";
            throw new AssertionError(errorMessage);
        }
        return secret;
    }
}
