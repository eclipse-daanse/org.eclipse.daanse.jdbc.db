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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import javax.sql.DataSource;

import org.eclipse.daanse.jdbc.db.api.DatabaseService;
import org.eclipse.daanse.jdbc.db.api.SqlStatementGenerator;
import org.eclipse.daanse.jdbc.db.api.meta.DatabaseInfo;
import org.eclipse.daanse.jdbc.db.api.meta.IdentifierInfo;
import org.eclipse.daanse.jdbc.db.api.meta.IndexInfo;
import org.eclipse.daanse.jdbc.db.api.meta.IndexInfoItem;
import org.eclipse.daanse.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.jdbc.db.api.meta.StructureInfo;
import org.eclipse.daanse.jdbc.db.api.meta.TypeInfo;
import org.eclipse.daanse.jdbc.db.api.meta.TypeInfo.Nullable;
import org.eclipse.daanse.jdbc.db.api.meta.TypeInfo.Searchable;
import org.eclipse.daanse.jdbc.db.api.schema.CatalogReference;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnReference;
import org.eclipse.daanse.jdbc.db.api.schema.ImportedKey;
import org.eclipse.daanse.jdbc.db.api.schema.SchemaReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.TableMetaData;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.eclipse.daanse.jdbc.db.record.meta.DatabaseInfoR;
import org.eclipse.daanse.jdbc.db.record.meta.IdentifierInfoR;
import org.eclipse.daanse.jdbc.db.record.meta.MetaInfoR;
import org.eclipse.daanse.jdbc.db.record.meta.StructureInfoR;
import org.eclipse.daanse.jdbc.db.record.meta.TypeInfoR;
import org.eclipse.daanse.jdbc.db.record.schema.CatalogReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnDefinitionR;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnMetaDataR;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.ImportedKeyR;
import org.eclipse.daanse.jdbc.db.record.schema.IndexInfoItemR;
import org.eclipse.daanse.jdbc.db.record.schema.IndexInfoR;
import org.eclipse.daanse.jdbc.db.record.schema.SchemaReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.TableDefinitionR;
import org.eclipse.daanse.jdbc.db.record.schema.TableMetaDataR;
import org.eclipse.daanse.jdbc.db.record.schema.TableReferenceR;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DatabaseService.class, scope = ServiceScope.SINGLETON)
public class DatabaseServiceImpl implements DatabaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseServiceImpl.class);
    private static final int NONUNIQUE_COLUMN = 4;
    private static final int TYPE_COLUMN = 7;
    private static final int COLUMN_NAME = 9;
    private static final int CARDINALITY_COLUMN = 11;

    private static final int[] RESULT_SET_TYPE_VALUES = {
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.TYPE_SCROLL_SENSITIVE};

        private static final int[] CONCURRENCY_VALUES = {
            ResultSet.CONCUR_READ_ONLY,
            ResultSet.CONCUR_UPDATABLE};

    @Override
    public MetaInfo createMetaInfo(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return createMetaInfo(connection);
        }
    }

    @Override
    public MetaInfo createMetaInfo(Connection connection) throws SQLException {
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        return readMetaInfo(databaseMetaData);
    }

    private MetaInfo readMetaInfo(DatabaseMetaData databaseMetaData) throws SQLException {
        DatabaseInfo databaseInfo = readDatabaseInfo(databaseMetaData);
        IdentifierInfo identifierInfo = readIdentifierInfo(databaseMetaData);
        List<TypeInfo> typeInfos = getTypeInfo(databaseMetaData);
        StructureInfo structureInfo = getStructureInfo(databaseMetaData);
        List<IndexInfo> indexInfos = getIndexInfo(databaseMetaData);
        return new MetaInfoR(databaseInfo, structureInfo, identifierInfo, typeInfos, indexInfos);
    }

    public List<IndexInfo> getIndexInfo(DatabaseMetaData databaseMetaData) throws SQLException{
        List<TableDefinition> tables = getTableDefinitions(databaseMetaData);
        List<IndexInfo> indexInfos = new ArrayList<>();
        for (TableDefinition tableDefinition : tables) {
            String catalog = null;
            String schema = null;
            TableReference table = tableDefinition.table();
            List<IndexInfoItem> indexInfoItems = new ArrayList<>();
            Optional<SchemaReference> oSchema = table.schema();
            if (oSchema.isPresent()) {
                SchemaReference sr = oSchema.get();
                schema = oSchema.get().name();
                if (sr.catalog().isPresent()) {
                    catalog = sr.catalog().get().name();
                }
            }
            try (ResultSet resultSet = databaseMetaData.getIndexInfo(catalog, schema, table.name(), false, true)) {
                while (resultSet.next()) {
                    int type = resultSet.getInt(TYPE_COLUMN);
                    String columnName = resultSet.getString(COLUMN_NAME);
                    int cardinalityColumn = resultSet.getInt(CARDINALITY_COLUMN);
                    boolean unique = !resultSet.getBoolean(NONUNIQUE_COLUMN);
                    indexInfoItems.add(new IndexInfoItemR(type, columnName, cardinalityColumn, unique));
                }
            }
            indexInfos.add(new IndexInfoR(table, indexInfoItems));
        }
        return List.copyOf(indexInfos);
    }

    private StructureInfo getStructureInfo(DatabaseMetaData databaseMetaData) throws SQLException {
        List<CatalogReference> catalogs = getCatalogs(databaseMetaData);
        List<SchemaReference> schemas = getSchemas(databaseMetaData);
        List<TableDefinition> tables = getTableDefinitions(databaseMetaData);
        List<ColumnDefinition> columns = getColumnDefinitions(databaseMetaData);

        List<ImportedKey> importedKeys = new ArrayList<ImportedKey>();
        for (TableDefinition tableDefinition : tables) {
            List<ImportedKey> iks = getImportedKeys(databaseMetaData, tableDefinition.table());
            importedKeys.addAll(iks);
        }

        StructureInfo structureInfo = new StructureInfoR(catalogs, schemas, tables, columns, importedKeys);
        return structureInfo;
    }

    @Override
    public List<CatalogReference> getCatalogs(DatabaseMetaData databaseMetaData) throws SQLException {

        List<CatalogReference> catalogs = new ArrayList<>();
        try (ResultSet rs = databaseMetaData.getCatalogs()) {
            while (rs.next()) {
                final String catalogName = rs.getString("TABLE_CAT");
                catalogs.add(new CatalogReferenceR(catalogName));
            }
        }
        return List.copyOf(catalogs);
    }

    @Override
    public List<SchemaReference> getSchemas(DatabaseMetaData databaseMetaData) throws SQLException {
        return getSchemas(databaseMetaData, null);
    }

    @Override
    public List<SchemaReference> getSchemas(DatabaseMetaData databaseMetaData, CatalogReference catalog)
            throws SQLException {

        String catalogName = null;

        if (catalog != null) {
            catalogName = catalog.name();
        }

        List<SchemaReference> schemas = new ArrayList<>();
        try (ResultSet rs = databaseMetaData.getSchemas(catalogName, null)) {
            while (rs.next()) {
                final String schemaName = rs.getString("TABLE_SCHEM");
                final Optional<CatalogReference> c = Optional.ofNullable(rs.getString("TABLE_CATALOG"))
                        .map(cat -> new CatalogReferenceR(cat));
                schemas.add(new SchemaReferenceR(c, schemaName));
            }
        }
        return List.copyOf(schemas);
    }

    @Override
    public List<String> getTableTypes(DatabaseMetaData databaseMetaData) throws SQLException {

        List<String> typeInfos = new ArrayList<>();
        try (ResultSet rs = databaseMetaData.getTableTypes()) {
            while (rs.next()) {
                final String tableTypeName = rs.getString("TABLE_TYPE");
                typeInfos.add(tableTypeName);
            }
        }

        return List.copyOf(typeInfos);
    }

    @Override
    public List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData) throws SQLException {
        return getTableDefinitions(databaseMetaData, List.of());
    }

    @Override
    public List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, List<String> types)
            throws SQLException {

        String[] typesArr = typesArrayForFilterNull(types);

        return getTableDefinitions(databaseMetaData, null, null, null, typesArr);
    }

    private static String[] typesArrayForFilterNull(List<String> types) {
        String[] typesArr = null;
        if (types != null && !types.isEmpty()) {
            typesArr = types.toArray(String[]::new);
        }
        return typesArr;
    }

    @Override
    public List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, CatalogReference catalog)
            throws SQLException {
        return getTableDefinitions(databaseMetaData, List.of());
    }

    @Override
    public List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, CatalogReference catalog,
            List<String> types) throws SQLException {
        String[] typesArr = typesArrayForFilterNull(types);
        return getTableDefinitions(databaseMetaData, catalog.name(), null, null, typesArr);
    }

    @Override
    public List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, SchemaReference schema)
            throws SQLException {
        return getTableDefinitions(databaseMetaData, schema, List.of());
    }

    @Override
    public List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, SchemaReference schema,
            List<String> types) throws SQLException {
        String[] typesArr = typesArrayForFilterNull(types);

        String catalog = schema.catalog().map(CatalogReference::name).orElse(null);
        return getTableDefinitions(databaseMetaData, catalog, schema.name(), null, typesArr);
    }

    @Override
    public List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, TableReference table)
            throws SQLException {

        Optional<SchemaReference> oSchema = table.schema();
        String schema = oSchema.map(SchemaReference::name).orElse(null);
        Optional<CatalogReference> oCatalog = oSchema.flatMap(SchemaReference::catalog);
        String catalog = oCatalog.map(CatalogReference::name).orElse(null);

        return getTableDefinitions(databaseMetaData, catalog, schema, table.name(), null);
    }

    @Override
    public List<TableDefinition> getTableDefinitions(DatabaseMetaData databaseMetaData, String catalog,
            String schemaPattern, String tableNamePattern, String types[]) throws SQLException {

        List<TableDefinition> tabeDefinitions = new ArrayList<>();
        try (ResultSet rs = databaseMetaData.getTables(catalog, schemaPattern, tableNamePattern, types)) {
            while (rs.next()) {
                final Optional<String> oCatalogName = Optional.ofNullable(rs.getString("TABLE_CAT"));
                final Optional<String> oSchemaName = Optional.ofNullable(rs.getString("TABLE_SCHEM"));
                final String tableName = rs.getString("TABLE_NAME");
                final String tableType = rs.getString("TABLE_TYPE");
                final Optional<String> oRemarks = Optional.ofNullable(rs.getString("REMARKS"));
                final Optional<String> oTypeCat = Optional.ofNullable(rs.getString("TYPE_CAT"));
                final Optional<String> oTypeSchema = Optional.ofNullable(rs.getString("TYPE_SCHEM"));
                final Optional<String> oTypeName = Optional.ofNullable(rs.getString("TYPE_NAME"));
                final Optional<String> oSelfRefColName = Optional.ofNullable(rs.getString("SELF_REFERENCING_COL_NAME"));
                final Optional<String> oRefGen = Optional.ofNullable(rs.getString("REF_GENERATION"));

                Optional<CatalogReference> oCatRef = oCatalogName.map(cn -> new CatalogReferenceR(cn));
                Optional<SchemaReference> oSchemaRef = oSchemaName.map(sn -> new SchemaReferenceR(oCatRef, sn));

                TableReference tableReference = new TableReferenceR(oSchemaRef, tableName, tableType);
                TableMetaData tableMetaData = new TableMetaDataR(oRemarks, oTypeCat, oTypeSchema, oTypeName,
                        oSelfRefColName, oRefGen);

                TableDefinition tableDefinition = new TableDefinitionR(tableReference, tableMetaData);
                tabeDefinitions.add(tableDefinition);
            }
        }

        return List.copyOf(tabeDefinitions);
    }

    @Override
    public boolean tableExists(DatabaseMetaData databaseMetaData, TableReference table) throws SQLException {

        Optional<SchemaReference> oSchema = table.schema();
        String schema = oSchema.map(SchemaReference::name).orElse(null);
        Optional<CatalogReference> oCatalog = oSchema.flatMap(SchemaReference::catalog);
        String catalog = oCatalog.map(CatalogReference::name).orElse(null);

        return tableExists(databaseMetaData, catalog, schema, table.name(), null);
    }

    @Override
    public boolean tableExists(DatabaseMetaData databaseMetaData, String catalog, String schemaPattern,
            String tableNamePattern, String types[]) throws SQLException {

        try (ResultSet rs = databaseMetaData.getTables(catalog, schemaPattern, tableNamePattern, types)) {
            return rs.next();
        }
    }

    @Override
    public List<TypeInfo> getTypeInfo(DatabaseMetaData databaseMetaData) throws SQLException {

        List<TypeInfo> typeInfos = new ArrayList<>();
        try (ResultSet rs = databaseMetaData.getTypeInfo()) {
            while (rs.next()) {
                final String typeName = rs.getString("TYPE_NAME");
                final int dataType = rs.getInt("DATA_TYPE");
                final int percision = rs.getInt("PRECISION");
                final Optional<String> literatPrefix = Optional.ofNullable(rs.getString("LITERAL_PREFIX"));
                final Optional<String> literatSuffix = Optional.ofNullable(rs.getString("LITERAL_SUFFIX"));
                final Optional<String> createPragmas = Optional.ofNullable(rs.getString("CREATE_PARAMS"));
                final Nullable nullable = TypeInfo.Nullable.of(rs.getShort("NULLABLE"));
                final boolean caseSensitive = rs.getBoolean("CASE_SENSITIVE");
                final Searchable searchable = TypeInfo.Searchable.of(rs.getShort("SEARCHABLE"));
                final boolean unsignesAttribute = rs.getBoolean("UNSIGNED_ATTRIBUTE");
                final boolean fixedPrecScale = rs.getBoolean("FIXED_PREC_SCALE");
                final boolean autoIncrement = rs.getBoolean("AUTO_INCREMENT");
                final Optional<String> localTypeName = Optional.ofNullable(rs.getString("LOCAL_TYPE_NAME"));
                final short minimumScale = rs.getShort("MINIMUM_SCALE");
                final short maximumScale = rs.getShort("MAXIMUM_SCALE");
                final int numPrecRadix = rs.getInt("NUM_PREC_RADIX");

                JDBCType jdbcType = JDBCType.valueOf(dataType);
                TypeInfoR typeInfo = new TypeInfoR(typeName, jdbcType, percision, literatPrefix, literatSuffix,
                        createPragmas, nullable, caseSensitive, searchable, unsignesAttribute, fixedPrecScale,
                        autoIncrement, localTypeName, minimumScale, maximumScale, numPrecRadix);
                typeInfos.add(typeInfo);
            }
        }

        return List.copyOf(typeInfos);
    }

    private static DatabaseInfo readDatabaseInfo(DatabaseMetaData databaseMetaData) {

        String productName = "";
        try {
            productName = databaseMetaData.getDatabaseProductName();
        } catch (SQLException e) {
            LOGGER.error("Exception while reading productName", e);
        }

        String productVersion = "";
        try {
            productVersion = databaseMetaData.getDatabaseProductVersion();
        } catch (SQLException e) {
            LOGGER.error("Exception while reading productVersion", e);
        }

        int majorVersion = 0;
        try {
            majorVersion = databaseMetaData.getDatabaseMajorVersion();
        } catch (SQLException e) {
            LOGGER.error("Exception while reading majorVersion", e);
        }

        int minorVersion = 0;
        try {
            minorVersion = databaseMetaData.getDatabaseMinorVersion();
        } catch (SQLException e) {
            LOGGER.error("Exception while reading minorVersion", e);
        }

        return new DatabaseInfoR(productName, productVersion, majorVersion, minorVersion);
    }

    private static IdentifierInfo readIdentifierInfo(DatabaseMetaData databaseMetaData) {

        String quoteString = " ";
        boolean readOnly = true;
        int maxColumnNameLength = 0;
        Set<List<Integer>> supportedResultSetStyles = Set.of();
        try {
            quoteString = databaseMetaData.getIdentifierQuoteString();
            maxColumnNameLength = databaseMetaData.getMaxColumnNameLength();
            readOnly = databaseMetaData.isReadOnly();
            supportedResultSetStyles = supportedResultSetStyles(databaseMetaData);
        } catch (SQLException e) {
            LOGGER.error("Exception while reading quoteString", e);
        }
        return new IdentifierInfoR(quoteString, maxColumnNameLength, readOnly, supportedResultSetStyles);
    }

    @Override
    public SqlStatementGenerator createSqlStatementGenerator(MetaInfo metaInfo) {
        return new SqlStatementGeneratorImpl(metaInfo);
    }

    @Override
    public List<ColumnDefinition> getColumnDefinitions(DatabaseMetaData databaseMetaData) throws SQLException {
        return getColumnDefinitions(databaseMetaData, null, null, null, null);

    }

    @Override
    public List<ColumnDefinition> getColumnDefinitions(DatabaseMetaData databaseMetaData, TableReference table)
            throws SQLException {

        String sTable = table.name();
        Optional<SchemaReference> oSchema = table.schema();
        String schema = oSchema.map(SchemaReference::name).orElse(null);
        Optional<CatalogReference> oCatalog = oSchema.flatMap(SchemaReference::catalog);
        String catalog = oCatalog.map(CatalogReference::name).orElse(null);

        return getColumnDefinitions(databaseMetaData, catalog, schema, sTable, null);
    }

    @Override
    public List<ColumnDefinition> getColumnDefinitions(DatabaseMetaData databaseMetaData, ColumnReference column)
            throws SQLException {

        Optional<TableReference> oTable = column.table();
        String table = oTable.map(TableReference::name).orElse(null);
        Optional<SchemaReference> oSchema = oTable.flatMap(TableReference::schema);
        String schema = oSchema.map(SchemaReference::name).orElse(null);
        Optional<CatalogReference> oCatalog = oSchema.flatMap(SchemaReference::catalog);
        String catalog = oCatalog.map(CatalogReference::name).orElse(null);

        return getColumnDefinitions(databaseMetaData, catalog, schema, table, column.name());
    }

    @Override
    public List<ColumnDefinition> getColumnDefinitions(DatabaseMetaData databaseMetaData, String catalog,
            String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        List<ColumnDefinition> columnDefinitions = new ArrayList<>();

        try (ResultSet rs = databaseMetaData.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);) {
            while (rs.next()) {

                final Optional<String> oCatalogName = Optional.ofNullable(rs.getString("TABLE_CAT"));
                final Optional<String> oSchemaName = Optional.ofNullable(rs.getString("TABLE_SCHEM"));
                final String tableName = rs.getString("TABLE_NAME");
                final String columName = rs.getString("COLUMN_NAME");

                final String typeName = rs.getString("TYPE_NAME");
                OptionalInt oColumnSize = OptionalInt.of(rs.getInt("COLUMN_SIZE"));

                if (rs.wasNull()) {
                    oColumnSize = OptionalInt.empty();
                }

                OptionalInt oDecimalDigits =  OptionalInt.of(rs.getInt("DECIMAL_DIGITS"));
                if (rs.wasNull()) {
                    oDecimalDigits = OptionalInt.empty();
                }

                OptionalInt oNumPrecRadix = OptionalInt.of(rs.getInt("NUM_PREC_RADIX"));
                if (rs.wasNull()) {
                    oNumPrecRadix=   OptionalInt.empty();
                }

                OptionalInt oNullable = OptionalInt.of( rs.getInt("NULLABLE"));
                if (rs.wasNull()) {
                    oNullable = OptionalInt.empty();
                }


                OptionalInt oCharOctetLength = OptionalInt.of( rs.getInt("CHAR_OCTET_LENGTH"));
                if (rs.wasNull()) {
                    oCharOctetLength = OptionalInt.empty();
                }


                final int dataType = rs.getInt("DATA_TYPE");

                final Optional<String> remarks = Optional.ofNullable(rs.getString("REMARKS"));

                Optional<CatalogReference> oCatRef = oCatalogName.map(cn -> new CatalogReferenceR(cn));
                Optional<SchemaReference> oSchemaRef = oSchemaName.map(sn -> new SchemaReferenceR(oCatRef, sn));

                JDBCType jdbcType = JDBCType.valueOf(dataType);
                TableReference tableReference = new TableReferenceR(oSchemaRef, tableName);

                ColumnReference columnReference = new ColumnReferenceR(Optional.of(tableReference), columName);
                ColumnDefinition columnDefinition = new ColumnDefinitionR(columnReference,
                        new ColumnMetaDataR(jdbcType,typeName, oColumnSize, oDecimalDigits,oNumPrecRadix,oNullable,oCharOctetLength, remarks));

                columnDefinitions.add(columnDefinition);
            }
        }
        return List.copyOf(columnDefinitions);
    }

    @Override
    public boolean columnExists(DatabaseMetaData databaseMetaData, ColumnReference column) throws SQLException {

        Optional<TableReference> oTable = column.table();
        String table = oTable.map(TableReference::name).orElse(null);
        Optional<SchemaReference> oSchema = oTable.flatMap(TableReference::schema);
        String schema = oSchema.map(SchemaReference::name).orElse(null);
        Optional<CatalogReference> oCatalog = oSchema.flatMap(SchemaReference::catalog);
        String catalog = oCatalog.map(CatalogReference::name).orElse(null);

        return columnExists(databaseMetaData, catalog, schema, table, column.name());
    }

    @Override
    public boolean columnExists(DatabaseMetaData databaseMetaData, String catalog, String schemaPattern,
            String tableNamePattern, String columnNamePattern) throws SQLException {

        try (ResultSet rs = databaseMetaData.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern)) {
            return rs.next();
        }
    }

    @Override
    public List<ImportedKey> getImportedKeys(DatabaseMetaData databaseMetaData, TableReference table)
            throws SQLException {

        Optional<SchemaReference> oSchema = table.schema();
        String schema = oSchema.map(SchemaReference::name).orElse(null);
        Optional<CatalogReference> oCatalog = oSchema.flatMap(SchemaReference::catalog);
        String catalog = oCatalog.map(CatalogReference::name).orElse(null);
        return getImportedKeys(databaseMetaData, catalog, schema, table.name());
    }

    @Override
    public List<ImportedKey> getImportedKeys(DatabaseMetaData databaseMetaData, String catalog, String schema,
            String tableName) throws SQLException {
        List<ImportedKey> importedKeys = new ArrayList<>();

        try (ResultSet rs = databaseMetaData.getImportedKeys(catalog, schema, tableName);) {
            while (rs.next()) {

                final Optional<String> oCatalogNamePK = Optional.ofNullable(rs.getString("PKTABLE_CAT"));
                final Optional<String> oSchemaNamePk = Optional.ofNullable(rs.getString("PKTABLE_SCHEM"));
                final String tableNamePk = rs.getString("PKTABLE_NAME");
                final String columNamePk = rs.getString("PKCOLUMN_NAME");

                final Optional<String> oCatalogNameFK = Optional.ofNullable(rs.getString("FKTABLE_CAT"));
                final Optional<String> oSchemaNameFk = Optional.ofNullable(rs.getString("FKTABLE_SCHEM"));
                final String tableNameFk = rs.getString("FKTABLE_NAME");
                final String columNameFk = rs.getString("FKCOLUMN_NAME");

                // PK
                Optional<CatalogReference> oCatRefPk = oCatalogNamePK.map(cn -> new CatalogReferenceR(cn));
                Optional<SchemaReference> oSchemaRefPk = oSchemaNamePk.map(sn -> new SchemaReferenceR(oCatRefPk, sn));
                TableReference tableReferencePk = new TableReferenceR(oSchemaRefPk, tableNamePk);
                ColumnReference primaryKeyColumn = new ColumnReferenceR(Optional.of(tableReferencePk), columNamePk);

                // FK
                Optional<CatalogReference> oCatRefFk = oCatalogNameFK.map(cn -> new CatalogReferenceR(cn));
                Optional<SchemaReference> oSchemaRefFk = oSchemaNameFk.map(sn -> new SchemaReferenceR(oCatRefFk, sn));
                TableReference tableReferenceFk = new TableReferenceR(oSchemaRefFk, tableNameFk);
                ColumnReference foreignKeyColumn = new ColumnReferenceR(Optional.of(tableReferenceFk), columNameFk);
                StringBuilder sb = new StringBuilder();
                sb.append("fk_").append(tableReferenceFk.name()).append("_").append(foreignKeyColumn.name()).append("_")
                        .append(tableReferencePk.name()).append("_").append(primaryKeyColumn.name());
                ImportedKey importedKey = new ImportedKeyR(primaryKeyColumn, foreignKeyColumn, sb.toString());
                importedKeys.add(importedKey);
            }
        }
        return List.copyOf(importedKeys);
    }

    private static Set<List<Integer>> supportedResultSetStyles(
        DatabaseMetaData databaseMetaData) throws SQLException
    {
        Set<List<Integer>> supports = new HashSet<>();
        for (int type : RESULT_SET_TYPE_VALUES) {
            for (int concurrency : CONCURRENCY_VALUES) {
                if (databaseMetaData.supportsResultSetConcurrency(
                    type, concurrency))
                {
                    String driverName =
                        databaseMetaData.getDriverName();
                    if (type != ResultSet.TYPE_FORWARD_ONLY
                        && driverName.equals(
                        "JDBC-ODBC Bridge (odbcjt32.dll)"))
                    {
                        // In JDK 1.6, the Jdbc-Odbc bridge announces
                        // that it can handle TYPE_SCROLL_INSENSITIVE
                        // but it does so by generating a 'COUNT(*)'
                        // query, and this query is invalid if the query
                        // contains a single-quote. So, override the
                        // driver.
                        continue;
                    }
                    supports.add(
                        new ArrayList<>(
                            Arrays.asList(type, concurrency)));
                }
            }
        }
        return supports;
    }

}
