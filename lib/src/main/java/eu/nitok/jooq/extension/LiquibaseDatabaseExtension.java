/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * ASL 2.0 and offer limited warranties, support, maintenance, and commercial
 * database integrations.
 *
 * For more information, please visit: http://www.jooq.org/licenses
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package eu.nitok.jooq.extension;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.*;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.meta.TableDefinition;
import org.jooq.tools.Convert;
import org.jooq.tools.JooqLogger;
import org.jooq.tools.StringUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

import static org.jooq.tools.StringUtils.isBlank;


public class LiquibaseDatabaseExtension {
    private final static JooqLogger          log = JooqLogger.getLogger(LiquibaseDatabaseExtension.class);
    private final SQLDialect sqlDialect;
    private final Supplier<Properties> getProperties;
    private String databaseChangeLogTableName;
    private String databaseChangeLogLockTableName;

    public LiquibaseDatabaseExtension(SQLDialect sqlDialect, Supplier<Properties> getProperties) {
        this.sqlDialect = sqlDialect;
        this.getProperties = getProperties;
    }


    public DSLContext provisionedConnection(Connection connection) throws Exception {
        var properties = getProperties.get();
        String rootPath = properties.getProperty("rootPath");
        String scripts = properties.getProperty("scripts");

        if (isBlank(scripts)) {
            scripts = "";
            log.warn("No scripts defined", "It is recommended that you provide an explicit script directory to scan");
        }
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        System.out.println("database created "+database.getConnection().getURL());
        String contexts = "";

        Map<String, Method> SETTERS = new HashMap<>();

        try {
            for (Method method : Database.class.getMethods()) {
                String name = method.getName();

                if (name.startsWith("set") && method.getParameterTypes().length == 1)
                    SETTERS.put(name, method);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // [#9514] Forward all database.xyz properties to matching Liquibase
        //         Database.setXyz() configuration setter calls
        for (Entry<Object, Object> entry : properties.entrySet()) {
            String key = "" + entry.getKey();
            if (key.startsWith("database.")) {
                String property = key.substring("database.".length());
                Method setter = SETTERS.get("set" + Character.toUpperCase(property.charAt(0)) + property.substring(1));

                try {
                    if (setter != null)
                        setter.invoke(database, Convert.convert(entry.getValue(), setter.getParameterTypes()[0]));
                }
                catch (Exception e) {
                    log.warn("Configuration error", e.getMessage(), e);
                }
            }

            // [#9872] Some changeLogParameters can also be passed along
            else if (key.startsWith("changeLogParameters.")) {
                String property = key.substring("changeLogParameters.".length());

                if ("contexts".equals(property))
                    contexts = "" + entry.getValue();
            }
        }

        // Retrieve changeLog table names as they might be overridden by configuration setters
        databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        databaseChangeLogLockTableName = database.getDatabaseChangeLogLockTableName();

        // [#9866] Allow for loading included files from the classpath or using absolute paths.
        // [#12872] [#13021] The decision is made based on the presence of the rootPath property
        ResourceAccessor ra = StringUtils.isBlank(rootPath)
                ? new CompositeResourceAccessor(
                new ClassLoaderResourceAccessor(),
                new ClassLoaderResourceAccessor(Thread.currentThread().getContextClassLoader())
        )
                : new FileSystemResourceAccessor(new File(rootPath));

        Liquibase liquibase = new Liquibase(scripts, ra, database);
        liquibase.update(contexts);
        return DSL.using(connection, sqlDialect);
    }


    public List<TableDefinition> getTables(List<TableDefinition> baseTables) {
        var result = new ArrayList<>(baseTables);

        if (!Boolean.parseBoolean(getProperties.get().getProperty("includeLiquibaseTables", "false"))) {
            List<String> liquibaseTables = Arrays.asList(databaseChangeLogTableName, databaseChangeLogLockTableName);
            result.removeIf(t -> liquibaseTables.contains(t.getName()));
        }

        return result;
    }
}