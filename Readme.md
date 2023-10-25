 # Jooq Liquibase-testcontainer extension
This jooq extension enables you to generate code from a liquibase changeset that is a applied to a real database and not only H2 as the official jooq-liquibase extension does.

This project is in its early stages - but it is still quite usable you only use one class

- **For JOOQ :** `3.16.x`
- **For Liquibase :** `4.23.x` (semi-optional)
> The liquibase version can be changed by importing into the same scope as long as it has no breaking changes