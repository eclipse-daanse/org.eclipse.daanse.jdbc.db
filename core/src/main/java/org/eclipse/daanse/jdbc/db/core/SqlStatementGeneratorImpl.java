/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.jdbc.db.core;

import java.util.Optional;

import org.eclipse.daanse.jdbc.db.api.SqlStatementGenerator;
import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.api.meta.TypeInfo;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnMetaData;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnReference;
import org.eclipse.daanse.jdbc.db.api.schema.ImportedKey;
import org.eclipse.daanse.jdbc.db.api.schema.Named;
import org.eclipse.daanse.jdbc.db.api.schema.TableDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.eclipse.daanse.jdbc.db.api.sql.AddColumnStatement;
import org.eclipse.daanse.jdbc.db.api.sql.CreateConstraintStatement;
import org.eclipse.daanse.jdbc.db.api.sql.CreateIndexStatement;
import org.eclipse.daanse.jdbc.db.api.sql.CreatePrimaryKeyStatement;
import org.eclipse.daanse.jdbc.db.api.sql.CreateSchemaSqlStatement;
import org.eclipse.daanse.jdbc.db.api.sql.CreateSqlStatement;
import org.eclipse.daanse.jdbc.db.api.sql.DeleteSqlStatement;
import org.eclipse.daanse.jdbc.db.api.sql.DropColumnStatement;
import org.eclipse.daanse.jdbc.db.api.sql.DropConstraintStatement;
import org.eclipse.daanse.jdbc.db.api.sql.DropContainerSqlStatement;
import org.eclipse.daanse.jdbc.db.api.sql.DropIndexStatement;
import org.eclipse.daanse.jdbc.db.api.sql.DropPrimaryKeyStatement;
import org.eclipse.daanse.jdbc.db.api.sql.DropSchemaSqlStatement;
import org.eclipse.daanse.jdbc.db.api.sql.InsertSqlStatement;
import org.eclipse.daanse.jdbc.db.api.sql.ModifyColumnStatement;
import org.eclipse.daanse.jdbc.db.api.sql.RenameColumnStatement;
import org.eclipse.daanse.jdbc.db.api.sql.RenameTableStatement;
import org.eclipse.daanse.jdbc.db.api.sql.SqlStatement;
import org.eclipse.daanse.jdbc.db.api.sql.TruncateTableSqlStatement;
import org.eclipse.daanse.jdbc.db.api.sql.UpdateSqlStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlStatementGeneratorImpl implements SqlStatementGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Logger.class);
    public static final String NO_QUOTE_FROM_METADATA = " ";

    private final MetaInfo metaInfo;
    private final String quoteString;

    public SqlStatementGeneratorImpl(MetaInfo metaInfo) {
        this.metaInfo = metaInfo;
        quoteString = metaInfo.identifierInfo().quoteString();
    }

    @Override
    public String getSqlOfStatement(SqlStatement statement) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Input SqlStatementObject: {}", statement);
        }

//Post Java 17
//        StringBuilder sb = switch (statement) {
//        case DropContainerSqlStatement dc -> writeDropContainerSqlStatement(dc);
//        case DropSchemaSqlStatement ds -> writeDropSchemaSqlStatement(ds);
//        case CreateSchemaSqlStatement cs -> writeCreateSchemaSqlStatement(cs);
//        case TruncateTableSqlStatement ts -> writeTruncateTableSqlStatement(ts);
//        case CreateSqlStatement cc -> writeCreateSqlStatement(cc);
//        case InsertSqlStatement is -> writeInsertSqlStatement(is);
//        case CreateConstraintStatement is -> writeCreateConstraintSqlStatement(is);
//        case DropConstraintStatement is -> writeDropConstraintSqlStatement(is);
//        };

        StringBuilder sb = null;
        if (statement instanceof DropContainerSqlStatement dc) {
            sb = writeDropContainerSqlStatement(dc);
        }else if (statement instanceof DropSchemaSqlStatement ds) {
            sb = writeDropSchemaSqlStatement(ds);
        }else if (statement instanceof CreateSchemaSqlStatement cs) {
            sb = writeCreateSchemaSqlStatement(cs);
        }else if (statement instanceof TruncateTableSqlStatement ts) {
            sb = writeTruncateTableSqlStatement(ts);
        }else if (statement instanceof CreateSqlStatement cc) {
            sb = writeCreateSqlStatement(cc);
        }else if (statement instanceof InsertSqlStatement is) {
            sb = writeInsertSqlStatement(is);
        }else if (statement instanceof CreateConstraintStatement ccs) {
            sb = writeCreateConstraintSqlStatement(ccs);
        }else if (statement instanceof DropConstraintStatement dc) {
            sb = writeDropConstraintSqlStatement(dc);
        }else if (statement instanceof CreateIndexStatement cis) {
            sb = writeCreateIndexStatement(cis);
        }else if (statement instanceof DropIndexStatement dis) {
            sb = writeDropIndexStatement(dis);
        }else if (statement instanceof CreatePrimaryKeyStatement cpks) {
            sb = writeCreatePrimaryKeyStatement(cpks);
        }else if (statement instanceof DropPrimaryKeyStatement dpks) {
            sb = writeDropPrimaryKeyStatement(dpks);
        }else if (statement instanceof AddColumnStatement acs) {
            sb = writeAddColumnStatement(acs);
        }else if (statement instanceof DropColumnStatement dcs) {
            sb = writeDropColumnStatement(dcs);
        }else if (statement instanceof ModifyColumnStatement mcs) {
            sb = writeModifyColumnStatement(mcs);
        }else if (statement instanceof RenameColumnStatement rcs) {
            sb = writeRenameColumnStatement(rcs);
        }else if (statement instanceof RenameTableStatement rts) {
            sb = writeRenameTableStatement(rts);
        }else if (statement instanceof UpdateSqlStatement uss) {
            sb = writeUpdateSqlStatement(uss);
        }else if (statement instanceof DeleteSqlStatement dss) {
            sb = writeDeleteSqlStatement(dss);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Generated SqlStatement: {}", sb.toString());
        }
        return sb.toString();
    }

    private StringBuilder writeDropConstraintSqlStatement(DropConstraintStatement statement) {
        //ALTER TABLE foo DROP CONSTRAINT IF EXISTS bar;
        TableReference table = statement.dropImportedKey().table();
        StringBuilder sb = new StringBuilder(20);
        sb.append("ALTER TABLE ").append(table.name());
        sb.append(" DROP CONSTRAINT ");
        if (statement.ifExists()) {
            sb.append("IF EXISTS ");
        }
        sb.append(statement.dropImportedKey().name());
        return sb;
    }

    private StringBuilder writeCreateConstraintSqlStatement(CreateConstraintStatement statement) {
        // ALTER TABLE table1 ADD CONSTRAINT `fk_table1_id1_table2_id2` FOREIGN KEY (`id1`) REFERENCES table2(`id2`)
        // ON DELETE NO ACTION ON UPDATE NO ACTION;
        ImportedKey ik = statement.importedKey();
        StringBuilder sb = new StringBuilder(20);
        sb.append("ALTER TABLE ").append(ik.foreignKeyColumn().table().get().name());
        sb.append(" ADD CONSTRAINT ");
        sb.append(ik.name());
        sb.append(" FOREIGN KEY (");
        sb.append(ik.foreignKeyColumn().name());
        sb.append(") REFERENCES ");
        sb.append(ik.primaryKeyColumn().table().get().name());
        sb.append("(").append(ik.primaryKeyColumn().name()).append(")");
        sb.append(" ON DELETE ").append(referentialActionToSql(ik.deleteRule()));
        sb.append(" ON UPDATE ").append(referentialActionToSql(ik.updateRule()));
        return sb;
    }

    private String referentialActionToSql(ImportedKey.ReferentialAction action) {
        return switch (action) {
            case CASCADE -> "CASCADE";
            case SET_NULL -> "SET NULL";
            case SET_DEFAULT -> "SET DEFAULT";
            case RESTRICT -> "RESTRICT";
            case NO_ACTION -> "NO ACTION";
        };
    }

    private StringBuilder writeCreateIndexStatement(CreateIndexStatement statement) {
        // CREATE [UNIQUE] INDEX [IF NOT EXISTS] index_name ON table (col1 [ASC|DESC], col2 [ASC|DESC], ...)
        StringBuilder sb = new StringBuilder(50);
        sb.append("CREATE ");
        if (statement.unique()) {
            sb.append("UNIQUE ");
        }
        sb.append("INDEX ");
        if (statement.ifNotExists()) {
            sb.append("IF NOT EXISTS ");
        }
        quoteIdentifier(sb, statement.indexName());
        sb.append(" ON ");
        quoteContainerReference(sb, statement.table());
        sb.append(" (");

        boolean first = true;
        for (CreateIndexStatement.IndexColumn column : statement.columns()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            quoteIdentifier(sb, column.columnName());
            if (column.ascending() != null) {
                sb.append(column.ascending() ? " ASC" : " DESC");
            }
        }
        sb.append(")");
        return sb;
    }

    private StringBuilder writeDropIndexStatement(DropIndexStatement statement) {
        // DROP INDEX [IF EXISTS] index_name [ON table]
        StringBuilder sb = new StringBuilder(30);
        sb.append("DROP INDEX ");
        if (statement.ifExists()) {
            sb.append("IF EXISTS ");
        }
        quoteIdentifier(sb, statement.indexName());

        // Some databases (MySQL) require ON table
        statement.table().ifPresent(table -> {
            sb.append(" ON ");
            quoteContainerReference(sb, table);
        });
        return sb;
    }

    private StringBuilder writeCreatePrimaryKeyStatement(CreatePrimaryKeyStatement statement) {
        // ALTER TABLE table ADD [CONSTRAINT constraint_name] PRIMARY KEY (col1, col2, ...)
        StringBuilder sb = new StringBuilder(50);
        sb.append("ALTER TABLE ");
        quoteContainerReference(sb, statement.table());
        sb.append(" ADD ");

        statement.constraintName().ifPresent(name -> {
            sb.append("CONSTRAINT ");
            quoteIdentifier(sb, name);
            sb.append(" ");
        });

        sb.append("PRIMARY KEY (");
        boolean first = true;
        for (String column : statement.columns()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            quoteIdentifier(sb, column);
        }
        sb.append(")");
        return sb;
    }

    private StringBuilder writeDropPrimaryKeyStatement(DropPrimaryKeyStatement statement) {
        // ALTER TABLE table DROP PRIMARY KEY
        // Or: ALTER TABLE table DROP CONSTRAINT constraint_name
        StringBuilder sb = new StringBuilder(40);
        sb.append("ALTER TABLE ");
        quoteContainerReference(sb, statement.table());

        if (statement.constraintName().isPresent()) {
            // Use DROP CONSTRAINT syntax (PostgreSQL, Oracle, SQL Server)
            sb.append(" DROP CONSTRAINT ");
            if (statement.ifExists()) {
                sb.append("IF EXISTS ");
            }
            quoteIdentifier(sb, statement.constraintName().get());
        } else {
            // Use DROP PRIMARY KEY syntax (MySQL)
            sb.append(" DROP PRIMARY KEY");
        }
        return sb;
    }

    private StringBuilder writeInsertSqlStatement(InsertSqlStatement statement) {
        TableReference table = statement.table();

        StringBuilder sb = new StringBuilder(20);
        sb.append("INSERT INTO ");

        quoteContainerReference(sb, table);

        sb.append("(");

        boolean firstColumn = true;
        for (ColumnReference column : statement.columns()) {

            if (firstColumn) {
                firstColumn = false;
            } else {
                sb.append(", ");

            }

            quoteReference(sb, column);
        }

        sb.append(") VALUES (");

        boolean firstValue = true;
        for (String value : statement.values()) {

            if (firstValue) {
                firstValue = false;
            } else {
                sb.append(", ");
            }
            sb.append(value);
        }

        sb.append(")");

        return sb;
    }

    private StringBuilder writeCreateSqlStatement(CreateSqlStatement statement) {
        TableDefinition table = statement.table();

        StringBuilder sb = new StringBuilder(20);
        sb.append("CREATE ");

        sb.append(table.table().type());
        sb.append(" ");

        if (statement.ifNotExists()) {
            sb.append("IF NOT EXISTS ");
        }

        quoteContainerReference(sb, table.table());

        sb.append("( ");

        boolean first = true;
        for (ColumnDefinition columnDefinition : statement.columnDefinitions()) {

            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            quoteReference(sb, columnDefinition.column());
            sb.append(" ");

            ColumnMetaData dataType = columnDefinition.columnMetaData();

            Optional<TypeInfo> oTypeInfo = metaInfo.typeInfos().stream().filter(t -> {
                return t.dataType() == dataType.dataType();
            }).findFirst();

            String typeName = oTypeInfo.map(TypeInfo::typeName).orElse(dataType.dataType().getName());
            sb.append(typeName);

            dataType.columnSize().ifPresent(columnSize -> {
                sb.append("(");
                sb.append(columnSize);

                dataType.decimalDigits().ifPresent(i -> {
                    sb.append(",");
                    sb.append(i);
                });

                sb.append(")");
            });

        }

        sb.append(")");

        return sb;
    }

    private StringBuilder writeTruncateTableSqlStatement(TruncateTableSqlStatement statement) {

        StringBuilder sb = new StringBuilder(30);
        sb.append("TRUNCATE TABLE ");
        quoteContainerReference(sb, statement.table());
        return sb;
    }

    private StringBuilder writeCreateSchemaSqlStatement(CreateSchemaSqlStatement statement) {

        StringBuilder sb = new StringBuilder(30);
        sb.append("CREATE SCHEMA ");

        if (statement.ifNotExists()) {
            sb.append("IF NOT EXISTS ");
        }
        quoteReference(sb, statement.schema());
        return sb;

    }

    private StringBuilder writeDropSchemaSqlStatement(DropSchemaSqlStatement statement) {

        StringBuilder sb = new StringBuilder(30);
        sb.append("DROP SCHEMA ");

        if (statement.ifExists()) {
            sb.append("IF EXISTS ");
        }
        quoteReference(sb, statement.schema());
        return sb;

    }

    private StringBuilder writeDropContainerSqlStatement(DropContainerSqlStatement statement) {
        TableReference table = statement.container();

        StringBuilder sb = new StringBuilder(20);
        sb.append("DROP ");

        sb.append(table.type());
        sb.append(" ");

        if (statement.ifExists()) {
            sb.append("IF EXISTS ");
        }

        quoteContainerReference(sb, table);

        return sb;
    }

    private void quoteContainerReference(final StringBuilder sb, final TableReference containerReference) {

        containerReference.schema().ifPresent(schema -> {
            quoteReference(sb, schema);
            sb.append(".");
        });
        quoteReference(sb, containerReference);
    }

    private void quoteReference(final StringBuilder sb, final Named reference) {
        quoteIdentifier(sb, reference.name());
    }

    private void quoteIdentifier(final StringBuilder sb, final String identifiert) {

        if ((quoteString.equals(NO_QUOTE_FROM_METADATA))
            || (identifiert.startsWith(quoteString) && identifiert.endsWith(quoteString))) {
            // no quote or already quoted
            sb.append(identifiert);
            return;
        }

        String cleanedIdentifier = identifiert.replace(quoteString, quoteString + quoteString);
        sb.append(quoteString);
        sb.append(cleanedIdentifier);
        sb.append(quoteString);
    }

    private StringBuilder writeAddColumnStatement(AddColumnStatement statement) {
        // ALTER TABLE table ADD [COLUMN] column_name data_type [NOT NULL] [DEFAULT value]
        StringBuilder sb = new StringBuilder(50);
        sb.append("ALTER TABLE ");
        quoteContainerReference(sb, statement.table());
        sb.append(" ADD COLUMN ");

        ColumnDefinition columnDefinition = statement.columnDefinition();
        quoteReference(sb, columnDefinition.column());
        sb.append(" ");

        ColumnMetaData dataType = columnDefinition.columnMetaData();

        Optional<TypeInfo> oTypeInfo = metaInfo.typeInfos().stream().filter(t -> {
            return t.dataType() == dataType.dataType();
        }).findFirst();

        String typeName = oTypeInfo.map(TypeInfo::typeName).orElse(dataType.dataType().getName());
        sb.append(typeName);

        dataType.columnSize().ifPresent(columnSize -> {
            sb.append("(");
            sb.append(columnSize);

            dataType.decimalDigits().ifPresent(i -> {
                sb.append(",");
                sb.append(i);
            });

            sb.append(")");
        });

        if (dataType.nullability() == ColumnMetaData.Nullability.NO_NULLS) {
            sb.append(" NOT NULL");
        }

        dataType.columnDefault().ifPresent(defaultVal -> {
            sb.append(" DEFAULT ");
            sb.append(defaultVal);
        });

        return sb;
    }

    private StringBuilder writeDropColumnStatement(DropColumnStatement statement) {
        // ALTER TABLE table DROP [COLUMN] [IF EXISTS] column_name
        StringBuilder sb = new StringBuilder(40);
        sb.append("ALTER TABLE ");
        quoteContainerReference(sb, statement.table());
        sb.append(" DROP COLUMN ");
        if (statement.ifExists()) {
            sb.append("IF EXISTS ");
        }
        quoteIdentifier(sb, statement.columnName());
        return sb;
    }

    private StringBuilder writeModifyColumnStatement(ModifyColumnStatement statement) {
        // ALTER TABLE table ALTER COLUMN column_name SET DATA TYPE new_data_type
        // Note: Syntax varies by database (MODIFY vs ALTER COLUMN)
        // Using standard SQL syntax here
        StringBuilder sb = new StringBuilder(60);
        sb.append("ALTER TABLE ");
        quoteContainerReference(sb, statement.table());
        sb.append(" ALTER COLUMN ");
        quoteIdentifier(sb, statement.columnName());
        sb.append(" SET DATA TYPE ");
        sb.append(statement.newDataType());

        if (!statement.nullable()) {
            sb.append(" NOT NULL");
        }

        statement.defaultValue().ifPresent(defaultVal -> {
            sb.append(" DEFAULT ");
            sb.append(defaultVal);
        });

        return sb;
    }

    private StringBuilder writeRenameColumnStatement(RenameColumnStatement statement) {
        // ALTER TABLE table RENAME COLUMN old_name TO new_name
        StringBuilder sb = new StringBuilder(50);
        sb.append("ALTER TABLE ");
        quoteContainerReference(sb, statement.table());
        sb.append(" RENAME COLUMN ");
        quoteIdentifier(sb, statement.oldColumnName());
        sb.append(" TO ");
        quoteIdentifier(sb, statement.newColumnName());
        return sb;
    }

    private StringBuilder writeRenameTableStatement(RenameTableStatement statement) {
        // ALTER TABLE old_name RENAME TO new_name
        StringBuilder sb = new StringBuilder(40);
        sb.append("ALTER TABLE ");
        quoteContainerReference(sb, statement.oldTable());
        sb.append(" RENAME TO ");
        quoteIdentifier(sb, statement.newTableName());
        return sb;
    }

    private StringBuilder writeUpdateSqlStatement(UpdateSqlStatement statement) {
        // UPDATE table SET column1=value1, column2=value2 [WHERE condition]
        StringBuilder sb = new StringBuilder(50);
        sb.append("UPDATE ");
        quoteContainerReference(sb, statement.table());
        sb.append(" SET ");

        boolean first = true;
        for (UpdateSqlStatement.ColumnValue cv : statement.setClause()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            quoteIdentifier(sb, cv.columnName());
            sb.append(" = ");
            sb.append(cv.value());
        }

        statement.whereClause().ifPresent(where -> {
            sb.append(" WHERE ");
            sb.append(where);
        });

        return sb;
    }

    private StringBuilder writeDeleteSqlStatement(DeleteSqlStatement statement) {
        // DELETE FROM table [WHERE condition]
        StringBuilder sb = new StringBuilder(40);
        sb.append("DELETE FROM ");
        quoteContainerReference(sb, statement.table());

        statement.whereClause().ifPresent(where -> {
            sb.append(" WHERE ");
            sb.append(where);
        });

        return sb;
    }

}
