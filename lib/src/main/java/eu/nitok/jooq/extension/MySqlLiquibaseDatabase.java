package eu.nitok.jooq.extension;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.meta.TableDefinition;
import org.jooq.meta.mysql.MySQLDatabase;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.SQLException;
import java.util.List;

public class MySqlLiquibaseDatabase extends MySQLDatabase {
    private final LiquibaseDatabaseExtension extension = new LiquibaseDatabaseExtension(SQLDialect.MYSQL, this::getProperties);

    @Override
    protected DSLContext create0() {
        try {
            return extension.provisionedConnection(getConnection());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<TableDefinition> getTables0() throws SQLException {
        return extension.getTables(super.getTables0());
    }
}
