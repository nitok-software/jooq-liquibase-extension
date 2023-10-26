package eu.nitok.jooq.liquibase;

import org.jooq.generated.your_schema_name.Tables;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CanAccessDbClasses {
	@Test
	void generatedTableReferencesContainCorrectSchema(){
		assertEquals("YOUR_SCHEMA_NAME", Tables.PERSON.getSchema().getName());
	}
}
