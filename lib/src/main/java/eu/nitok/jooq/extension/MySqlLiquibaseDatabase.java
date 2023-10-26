package eu.nitok.jooq.extension;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.meta.TableDefinition;
import org.jooq.meta.mysql.MySQLDatabase;

import java.sql.SQLException;
import java.util.List;

public class MySqlLiquibaseDatabase extends MySQLDatabase {
	private final LiquibaseDatabaseExtension extension = new LiquibaseDatabaseExtension(
		SQLDialect.MYSQL,
		this::getProperties
	);
	private DSLContext dslContext;

	@Override
	protected List<TableDefinition> getTables0() throws SQLException {
		return extension.getTables(super.getTables0());
	}

	@Override
	protected DSLContext create0() {
		try {
			if (dslContext == null) dslContext = extension.provisionedConnection(getConnection());
			return dslContext;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
