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
import org.eclipse.daanse.jdbc.db.api.schema.ColumnMetaData;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnReference;
import org.eclipse.daanse.jdbc.db.api.schema.Function;
import org.eclipse.daanse.jdbc.db.api.schema.FunctionColumn;
import org.eclipse.daanse.jdbc.db.api.schema.ImportedKey;
import org.eclipse.daanse.jdbc.db.api.schema.PrimaryKey;
import org.eclipse.daanse.jdbc.db.api.schema.Procedure;
import org.eclipse.daanse.jdbc.db.api.schema.ProcedureColumn;
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
import org.eclipse.daanse.jdbc.db.record.schema.FunctionColumnR;
import org.eclipse.daanse.jdbc.db.record.schema.FunctionR;
import org.eclipse.daanse.jdbc.db.record.schema.FunctionReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.ImportedKeyR;
import org.eclipse.daanse.jdbc.db.record.schema.IndexInfoItemR;
import org.eclipse.daanse.jdbc.db.record.schema.IndexInfoR;
import org.eclipse.daanse.jdbc.db.record.schema.PrimaryKeyR;
import org.eclipse.daanse.jdbc.db.record.schema.ProcedureColumnR;
import org.eclipse.daanse.jdbc.db.record.schema.ProcedureR;
import org.eclipse.daanse.jdbc.db.record.schema.ProcedureReferenceR;
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

    // Index info column positions (from JDBC spec)
    private static final int INDEX_NON_UNIQUE = 4;
    private static final int INDEX_NAME = 6;
    private static final int INDEX_TYPE = 7;
    private static final int INDEX_ORDINAL_POSITION = 8;
    private static final int INDEX_COLUMN_NAME = 9;
    private static final int INDEX_ASC_OR_DESC = 10;
    private static final int INDEX_CARDINALITY = 11;
    private static final int INDEX_PAGES = 12;
    private static final int INDEX_FILTER_CONDITION = 13;

    private static final int[] RESULT_SET_TYPE_VALUES = { ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE };

    private static final int[] CONCURRENCY_VALUES = { ResultSet.CONCUR_READ_ONLY, ResultSet.CONCUR_UPDATABLE };

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

    public List<IndexInfo> getIndexInfo(DatabaseMetaData databaseMetaData) throws SQLException {
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
            LOGGER.debug("Reading index info for table: {}.{}.{}", catalog, schema, table.name());
            try (ResultSet resultSet = databaseMetaData.getIndexInfo(catalog, schema, table.name(), false, true)) {
                while (resultSet.next()) {
                    boolean nonUnique = resultSet.getBoolean(INDEX_NON_UNIQUE);
                    Optional<String> indexName = Optional.ofNullable(resultSet.getString(INDEX_NAME));
                    int type = resultSet.getInt(INDEX_TYPE);
                    int ordinalPosition = resultSet.getInt(INDEX_ORDINAL_POSITION);
                    Optional<String> columnName = Optional.ofNullable(resultSet.getString(INDEX_COLUMN_NAME));
                    String ascOrDesc = resultSet.getString(INDEX_ASC_OR_DESC);
                    Optional<Boolean> ascending = ascOrDesc == null ? Optional.empty() :
                            Optional.of("A".equalsIgnoreCase(ascOrDesc));
                    long cardinality = resultSet.getLong(INDEX_CARDINALITY);
                    long pages = resultSet.getLong(INDEX_PAGES);
                    Optional<String> filterCondition = Optional.ofNullable(resultSet.getString(INDEX_FILTER_CONDITION));

                    IndexInfoItem.IndexType indexType = IndexInfoItem.IndexType.of(type);
                    indexInfoItems.add(new IndexInfoItemR(indexName, indexType, columnName, ordinalPosition,
                            ascending, cardinality, pages, filterCondition, !nonUnique));
                }
            } catch (SQLException e) {
                LOGGER.warn("Error reading index info for table: {}.{}.{} - {}", catalog, schema, table.name(),
                        e.getMessage());

                continue;
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
        List<PrimaryKey> primaryKeys = new ArrayList<PrimaryKey>();
        for (TableDefinition tableDefinition : tables) {
            List<ImportedKey> iks = getImportedKeys(databaseMetaData, tableDefinition.table());
            importedKeys.addAll(iks);

            PrimaryKey pk = getPrimaryKey(databaseMetaData, tableDefinition.table());
            if (pk != null) {
                primaryKeys.add(pk);
            }
        }

        StructureInfo structureInfo = new StructureInfoR(catalogs, schemas, tables, columns, importedKeys, primaryKeys);
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
            int columnCount = rs.getMetaData().getColumnCount();
            Set<String> columnNames = new HashSet<>();
            for (int i = 1; i <= columnCount; i++) {

                String colName = rs.getMetaData().getColumnName(i);
                columnNames.add(colName);
            }
            while (rs.next()) {
                final Optional<String> oCatalogName = Optional.ofNullable(rs.getString("TABLE_CAT"));
                final Optional<String> oSchemaName = Optional.ofNullable(rs.getString("TABLE_SCHEM"));
                final String tableName = rs.getString("TABLE_NAME");
                final String tableType = rs.getString("TABLE_TYPE");
                final Optional<String> oRemarks = Optional.ofNullable(rs.getString("REMARKS"));

                final Optional<String> oTypeCat = getColumnValue(rs, columnNames, "TYPE_CAT");
                final Optional<String> oTypeSchema = getColumnValue(rs, columnNames, "TYPE_SCHEM");
                final Optional<String> oTypeName = getColumnValue(rs, columnNames, "TYPE_NAME");
                final Optional<String> oSelfRefColName = getColumnValue(rs, columnNames, "SELF_REFERENCING_COL_NAME");
                final Optional<String> oRefGen = getColumnValue(rs, columnNames, "REF_GENERATION");

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

    private Optional<String> getColumnValue(ResultSet rs, Set<String> columnNames, String columnName)
            throws SQLException {
        if (!columnNames.contains(columnName)) {
            return Optional.empty();
        }
        String value = rs.getString(columnName);
        if (rs.wasNull()) {
            return Optional.empty();
        } else {
            return Optional.of(value);
        }
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
                final int precision = rs.getInt("PRECISION");
                final Optional<String> literalPrefix = Optional.ofNullable(rs.getString("LITERAL_PREFIX"));
                final Optional<String> literalSuffix = Optional.ofNullable(rs.getString("LITERAL_SUFFIX"));
                final Optional<String> createParams = Optional.ofNullable(rs.getString("CREATE_PARAMS"));
                final Nullable nullable = TypeInfo.Nullable.of(rs.getShort("NULLABLE"));
                final boolean caseSensitive = rs.getBoolean("CASE_SENSITIVE");
                final Searchable searchable = TypeInfo.Searchable.of(rs.getShort("SEARCHABLE"));
                final boolean unsignedAttribute = rs.getBoolean("UNSIGNED_ATTRIBUTE");
                final boolean fixedPrecScale = rs.getBoolean("FIXED_PREC_SCALE");
                final boolean autoIncrement = rs.getBoolean("AUTO_INCREMENT");
                final Optional<String> localTypeName = Optional.ofNullable(rs.getString("LOCAL_TYPE_NAME"));
                final short minimumScale = rs.getShort("MINIMUM_SCALE");
                final short maximumScale = rs.getShort("MAXIMUM_SCALE");
                final int numPrecRadix = rs.getInt("NUM_PREC_RADIX");

                JDBCType jdbcType;
                try {
                    jdbcType = JDBCType.valueOf(dataType);
                } catch (IllegalArgumentException ex) {
                    jdbcType = JDBCType.OTHER;
                    LOGGER.info("Unknown JDBC-Typcode: " + dataType + " (" + typeName + ")");
                }
                TypeInfoR typeInfo = new TypeInfoR(typeName, jdbcType, precision, literalPrefix, literalSuffix,
                        createParams, nullable, caseSensitive, searchable, unsignedAttribute, fixedPrecScale,
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

                OptionalInt oDecimalDigits = OptionalInt.of(rs.getInt("DECIMAL_DIGITS"));
                if (rs.wasNull()) {
                    oDecimalDigits = OptionalInt.empty();
                }

                OptionalInt oNumPrecRadix = OptionalInt.of(rs.getInt("NUM_PREC_RADIX"));
                if (rs.wasNull()) {
                    oNumPrecRadix = OptionalInt.empty();
                }

                OptionalInt oNullable = OptionalInt.of(rs.getInt("NULLABLE"));
                if (rs.wasNull()) {
                    oNullable = OptionalInt.empty();
                }

                OptionalInt oCharOctetLength = OptionalInt.of(rs.getInt("CHAR_OCTET_LENGTH"));
                if (rs.wasNull()) {
                    oCharOctetLength = OptionalInt.empty();
                }

                final int dataType = rs.getInt("DATA_TYPE");

                final Optional<String> remarks = Optional.ofNullable(rs.getString("REMARKS"));

                // Additional fields from JDBC spec
                final Optional<String> columnDefault = Optional.ofNullable(rs.getString("COLUMN_DEF"));
                final ColumnMetaData.Nullability nullability = oNullable.isPresent()
                        ? ColumnMetaData.Nullability.of(oNullable.getAsInt())
                        : ColumnMetaData.Nullability.UNKNOWN;

                // IS_AUTOINCREMENT and IS_GENERATEDCOLUMN may not be available in all drivers
                ColumnMetaData.AutoIncrement autoIncrement = ColumnMetaData.AutoIncrement.UNKNOWN;
                ColumnMetaData.GeneratedColumn generatedColumn = ColumnMetaData.GeneratedColumn.UNKNOWN;
                try {
                    String isAutoIncrement = rs.getString("IS_AUTOINCREMENT");
                    autoIncrement = ColumnMetaData.AutoIncrement.ofString(isAutoIncrement);
                } catch (SQLException e) {
                    LOGGER.debug("IS_AUTOINCREMENT not available for column: {}.{}", tableName, columName);
                }
                try {
                    String isGeneratedColumn = rs.getString("IS_GENERATEDCOLUMN");
                    generatedColumn = ColumnMetaData.GeneratedColumn.ofString(isGeneratedColumn);
                } catch (SQLException e) {
                    LOGGER.debug("IS_GENERATEDCOLUMN not available for column: {}.{}", tableName, columName);
                }

                Optional<CatalogReference> oCatRef = oCatalogName.map(cn -> new CatalogReferenceR(cn));
                Optional<SchemaReference> oSchemaRef = oSchemaName.map(sn -> new SchemaReferenceR(oCatRef, sn));

                JDBCType jdbcType;
                try {
                    jdbcType = JDBCType.valueOf(dataType);
                } catch (IllegalArgumentException ex) {
                    jdbcType = JDBCType.OTHER;
                    LOGGER.info("Unknown JDBC-Typcode: " + dataType + " (" + typeName + ") in table: " + tableName
                            + " column: " + columName);
                }

                TableReference tableReference = new TableReferenceR(oSchemaRef, tableName);

                ColumnReference columnReference = new ColumnReferenceR(Optional.of(tableReference), columName);
                ColumnDefinition columnDefinition = new ColumnDefinitionR(columnReference, new ColumnMetaDataR(
                        jdbcType, typeName, oColumnSize, oDecimalDigits, oNumPrecRadix, oNullable, nullability,
                        oCharOctetLength, remarks, columnDefault, autoIncrement, generatedColumn));

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

                // Additional fields from JDBC spec
                final int keySeq = rs.getInt("KEY_SEQ");
                final int updateRule = rs.getInt("UPDATE_RULE");
                final int deleteRule = rs.getInt("DELETE_RULE");
                final String fkName = rs.getString("FK_NAME");
                final Optional<String> pkName = Optional.ofNullable(rs.getString("PK_NAME"));
                final int deferrability = rs.getInt("DEFERRABILITY");

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

                // Use FK_NAME from database if available, otherwise generate one
                String constraintName;
                if (fkName != null && !fkName.isBlank()) {
                    constraintName = fkName;
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("fk_").append(tableReferenceFk.name()).append("_").append(foreignKeyColumn.name())
                            .append("_").append(tableReferencePk.name()).append("_").append(primaryKeyColumn.name());
                    constraintName = sb.toString();
                }

                ImportedKey importedKey = new ImportedKeyR(
                        primaryKeyColumn,
                        foreignKeyColumn,
                        constraintName,
                        keySeq,
                        ImportedKey.ReferentialAction.of(updateRule),
                        ImportedKey.ReferentialAction.of(deleteRule),
                        pkName,
                        ImportedKey.Deferrability.of(deferrability));
                importedKeys.add(importedKey);
            }
        }
        return List.copyOf(importedKeys);
    }

    /**
     * Gets the primary key for a table.
     *
     * @param databaseMetaData the database metadata
     * @param table the table reference
     * @return the primary key, or null if the table has no primary key
     * @throws SQLException if a database access error occurs
     */
    public PrimaryKey getPrimaryKey(DatabaseMetaData databaseMetaData, TableReference table) throws SQLException {
        Optional<SchemaReference> oSchema = table.schema();
        String schema = oSchema.map(SchemaReference::name).orElse(null);
        Optional<CatalogReference> oCatalog = oSchema.flatMap(SchemaReference::catalog);
        String catalog = oCatalog.map(CatalogReference::name).orElse(null);

        List<ColumnReference> columns = new ArrayList<>();
        String pkName = null;

        try (ResultSet rs = databaseMetaData.getPrimaryKeys(catalog, schema, table.name())) {
            // Results are ordered by COLUMN_NAME, but we need to order by KEY_SEQ
            // So we collect all columns first
            java.util.TreeMap<Integer, ColumnReference> orderedColumns = new java.util.TreeMap<>();

            while (rs.next()) {
                final String columnName = rs.getString("COLUMN_NAME");
                final int keySeq = rs.getInt("KEY_SEQ");
                pkName = rs.getString("PK_NAME"); // Same for all rows

                ColumnReference colRef = new ColumnReferenceR(Optional.of(table), columnName);
                orderedColumns.put(keySeq, colRef);
            }

            // Add columns in KEY_SEQ order
            columns.addAll(orderedColumns.values());
        }

        if (columns.isEmpty()) {
            return null; // No primary key
        }

        return new PrimaryKeyR(table, List.copyOf(columns), Optional.ofNullable(pkName));
    }

    private static Set<List<Integer>> supportedResultSetStyles(DatabaseMetaData databaseMetaData) throws SQLException {
        Set<List<Integer>> supports = new HashSet<>();
        for (int type : RESULT_SET_TYPE_VALUES) {
            for (int concurrency : CONCURRENCY_VALUES) {
                if (databaseMetaData.supportsResultSetConcurrency(type, concurrency)) {
                    String driverName = databaseMetaData.getDriverName();
                    if (type != ResultSet.TYPE_FORWARD_ONLY && driverName.equals("JDBC-ODBC Bridge (odbcjt32.dll)")) {
                        // In JDK 1.6, the Jdbc-Odbc bridge announces
                        // that it can handle TYPE_SCROLL_INSENSITIVE
                        // but it does so by generating a 'COUNT(*)'
                        // query, and this query is invalid if the query
                        // contains a single-quote. So, override the
                        // driver.
                        continue;
                    }
                    supports.add(new ArrayList<>(Arrays.asList(type, concurrency)));
                }
            }
        }
        return supports;
    }

    @Override
    public List<Procedure> getProcedures(DatabaseMetaData databaseMetaData) throws SQLException {
        return getProcedures(databaseMetaData, null, null, null);
    }

    @Override
    public List<Procedure> getProcedures(DatabaseMetaData databaseMetaData, String catalog, String schemaPattern,
            String procedureNamePattern) throws SQLException {
        List<Procedure> procedures = new ArrayList<>();

        try (ResultSet rs = databaseMetaData.getProcedures(catalog, schemaPattern, procedureNamePattern)) {
            while (rs.next()) {
                final Optional<String> oCatalogName = Optional.ofNullable(rs.getString("PROCEDURE_CAT"));
                final Optional<String> oSchemaName = Optional.ofNullable(rs.getString("PROCEDURE_SCHEM"));
                final String procedureName = rs.getString("PROCEDURE_NAME");
                final Optional<String> remarks = Optional.ofNullable(rs.getString("REMARKS"));
                final int procedureType = rs.getInt("PROCEDURE_TYPE");
                final String specificName = rs.getString("SPECIFIC_NAME");

                Optional<CatalogReference> oCatRef = oCatalogName.map(CatalogReferenceR::new);
                Optional<SchemaReference> oSchemaRef = oSchemaName.map(sn -> new SchemaReferenceR(oCatRef, sn));

                ProcedureReferenceR reference = new ProcedureReferenceR(oSchemaRef, procedureName, specificName);

                // Get procedure columns
                List<ProcedureColumn> columns = getProcedureColumns(databaseMetaData,
                        oCatalogName.orElse(null), oSchemaName.orElse(null), procedureName);

                Procedure procedure = new ProcedureR(reference, Procedure.ProcedureType.of(procedureType), remarks, columns);
                procedures.add(procedure);
            }
        }

        return List.copyOf(procedures);
    }

    private List<ProcedureColumn> getProcedureColumns(DatabaseMetaData databaseMetaData, String catalog,
            String schema, String procedureName) throws SQLException {
        List<ProcedureColumn> columns = new ArrayList<>();

        try (ResultSet rs = databaseMetaData.getProcedureColumns(catalog, schema, procedureName, null)) {
            while (rs.next()) {
                final String columnName = rs.getString("COLUMN_NAME");
                final int columnType = rs.getInt("COLUMN_TYPE");
                final int dataType = rs.getInt("DATA_TYPE");
                final String typeName = rs.getString("TYPE_NAME");

                OptionalInt precision = OptionalInt.of(rs.getInt("PRECISION"));
                if (rs.wasNull()) {
                    precision = OptionalInt.empty();
                }

                OptionalInt scale = OptionalInt.of(rs.getInt("SCALE"));
                if (rs.wasNull()) {
                    scale = OptionalInt.empty();
                }

                OptionalInt radix = OptionalInt.of(rs.getInt("RADIX"));
                if (rs.wasNull()) {
                    radix = OptionalInt.empty();
                }

                final int nullable = rs.getInt("NULLABLE");
                final Optional<String> remarks = Optional.ofNullable(rs.getString("REMARKS"));
                final Optional<String> columnDefault = Optional.ofNullable(rs.getString("COLUMN_DEF"));
                final int ordinalPosition = rs.getInt("ORDINAL_POSITION");

                JDBCType jdbcType;
                try {
                    jdbcType = JDBCType.valueOf(dataType);
                } catch (IllegalArgumentException ex) {
                    jdbcType = JDBCType.OTHER;
                    LOGGER.debug("Unknown JDBC type code: {} ({}) for procedure column: {}.{}",
                            dataType, typeName, procedureName, columnName);
                }

                ProcedureColumn column = new ProcedureColumnR(columnName, ProcedureColumn.ColumnType.of(columnType),
                        jdbcType, typeName, precision, scale, radix, ProcedureColumn.Nullability.of(nullable),
                        remarks, columnDefault, ordinalPosition);
                columns.add(column);
            }
        }

        return List.copyOf(columns);
    }

    @Override
    public List<Function> getFunctions(DatabaseMetaData databaseMetaData) throws SQLException {
        return getFunctions(databaseMetaData, null, null, null);
    }

    @Override
    public List<Function> getFunctions(DatabaseMetaData databaseMetaData, String catalog, String schemaPattern,
            String functionNamePattern) throws SQLException {
        List<Function> functions = new ArrayList<>();

        try (ResultSet rs = databaseMetaData.getFunctions(catalog, schemaPattern, functionNamePattern)) {
            while (rs.next()) {
                final Optional<String> oCatalogName = Optional.ofNullable(rs.getString("FUNCTION_CAT"));
                final Optional<String> oSchemaName = Optional.ofNullable(rs.getString("FUNCTION_SCHEM"));
                final String functionName = rs.getString("FUNCTION_NAME");
                final Optional<String> remarks = Optional.ofNullable(rs.getString("REMARKS"));
                final int functionType = rs.getInt("FUNCTION_TYPE");
                final String specificName = rs.getString("SPECIFIC_NAME");

                Optional<CatalogReference> oCatRef = oCatalogName.map(CatalogReferenceR::new);
                Optional<SchemaReference> oSchemaRef = oSchemaName.map(sn -> new SchemaReferenceR(oCatRef, sn));

                FunctionReferenceR reference = new FunctionReferenceR(oSchemaRef, functionName, specificName);

                // Get function columns
                List<FunctionColumn> columns = getFunctionColumns(databaseMetaData,
                        oCatalogName.orElse(null), oSchemaName.orElse(null), functionName);

                Function function = new FunctionR(reference, Function.FunctionType.of(functionType), remarks, columns);
                functions.add(function);
            }
        }

        return List.copyOf(functions);
    }

    private List<FunctionColumn> getFunctionColumns(DatabaseMetaData databaseMetaData, String catalog,
            String schema, String functionName) throws SQLException {
        List<FunctionColumn> columns = new ArrayList<>();

        try (ResultSet rs = databaseMetaData.getFunctionColumns(catalog, schema, functionName, null)) {
            while (rs.next()) {
                final String columnName = rs.getString("COLUMN_NAME");
                final int columnType = rs.getInt("COLUMN_TYPE");
                final int dataType = rs.getInt("DATA_TYPE");
                final String typeName = rs.getString("TYPE_NAME");

                OptionalInt precision = OptionalInt.of(rs.getInt("PRECISION"));
                if (rs.wasNull()) {
                    precision = OptionalInt.empty();
                }

                OptionalInt scale = OptionalInt.of(rs.getInt("SCALE"));
                if (rs.wasNull()) {
                    scale = OptionalInt.empty();
                }

                OptionalInt radix = OptionalInt.of(rs.getInt("RADIX"));
                if (rs.wasNull()) {
                    radix = OptionalInt.empty();
                }

                final int nullable = rs.getInt("NULLABLE");
                final Optional<String> remarks = Optional.ofNullable(rs.getString("REMARKS"));

                OptionalInt charOctetLength = OptionalInt.of(rs.getInt("CHAR_OCTET_LENGTH"));
                if (rs.wasNull()) {
                    charOctetLength = OptionalInt.empty();
                }

                final int ordinalPosition = rs.getInt("ORDINAL_POSITION");

                JDBCType jdbcType;
                try {
                    jdbcType = JDBCType.valueOf(dataType);
                } catch (IllegalArgumentException ex) {
                    jdbcType = JDBCType.OTHER;
                    LOGGER.debug("Unknown JDBC type code: {} ({}) for function column: {}.{}",
                            dataType, typeName, functionName, columnName);
                }

                FunctionColumn column = new FunctionColumnR(columnName, FunctionColumn.ColumnType.of(columnType),
                        jdbcType, typeName, precision, scale, radix, FunctionColumn.Nullability.of(nullable),
                        remarks, charOctetLength, ordinalPosition);
                columns.add(column);
            }
        }

        return List.copyOf(columns);
    }

    @Override
    public List<ImportedKey> getExportedKeys(DatabaseMetaData databaseMetaData, TableReference table)
            throws SQLException {
        Optional<SchemaReference> oSchema = table.schema();
        String schema = oSchema.map(SchemaReference::name).orElse(null);
        Optional<CatalogReference> oCatalog = oSchema.flatMap(SchemaReference::catalog);
        String catalog = oCatalog.map(CatalogReference::name).orElse(null);
        return getExportedKeys(databaseMetaData, catalog, schema, table.name());
    }

    @Override
    public List<ImportedKey> getExportedKeys(DatabaseMetaData databaseMetaData, String catalog, String schema,
            String tableName) throws SQLException {
        List<ImportedKey> exportedKeys = new ArrayList<>();

        try (ResultSet rs = databaseMetaData.getExportedKeys(catalog, schema, tableName)) {
            while (rs.next()) {
                ImportedKey key = readForeignKeyFromResultSet(rs);
                exportedKeys.add(key);
            }
        }
        return List.copyOf(exportedKeys);
    }

    @Override
    public List<ImportedKey> getCrossReference(DatabaseMetaData databaseMetaData, TableReference parentTable,
            TableReference foreignTable) throws SQLException {
        Optional<SchemaReference> parentSchema = parentTable.schema();
        String pSchema = parentSchema.map(SchemaReference::name).orElse(null);
        Optional<CatalogReference> parentCatalog = parentSchema.flatMap(SchemaReference::catalog);
        String pCatalog = parentCatalog.map(CatalogReference::name).orElse(null);

        Optional<SchemaReference> foreignSchema = foreignTable.schema();
        String fSchema = foreignSchema.map(SchemaReference::name).orElse(null);
        Optional<CatalogReference> foreignCatalog = foreignSchema.flatMap(SchemaReference::catalog);
        String fCatalog = foreignCatalog.map(CatalogReference::name).orElse(null);

        return getCrossReference(databaseMetaData, pCatalog, pSchema, parentTable.name(),
                fCatalog, fSchema, foreignTable.name());
    }

    @Override
    public List<ImportedKey> getCrossReference(DatabaseMetaData databaseMetaData, String parentCatalog,
            String parentSchema, String parentTable, String foreignCatalog, String foreignSchema,
            String foreignTable) throws SQLException {
        List<ImportedKey> crossRefs = new ArrayList<>();

        try (ResultSet rs = databaseMetaData.getCrossReference(parentCatalog, parentSchema, parentTable,
                foreignCatalog, foreignSchema, foreignTable)) {
            while (rs.next()) {
                ImportedKey key = readForeignKeyFromResultSet(rs);
                crossRefs.add(key);
            }
        }
        return List.copyOf(crossRefs);
    }

    /**
     * Helper method to read a foreign key relationship from a ResultSet.
     * Used by getImportedKeys, getExportedKeys, and getCrossReference since
     * they all return the same column structure.
     */
    private ImportedKey readForeignKeyFromResultSet(ResultSet rs) throws SQLException {
        final Optional<String> oCatalogNamePK = Optional.ofNullable(rs.getString("PKTABLE_CAT"));
        final Optional<String> oSchemaNamePk = Optional.ofNullable(rs.getString("PKTABLE_SCHEM"));
        final String tableNamePk = rs.getString("PKTABLE_NAME");
        final String columNamePk = rs.getString("PKCOLUMN_NAME");

        final Optional<String> oCatalogNameFK = Optional.ofNullable(rs.getString("FKTABLE_CAT"));
        final Optional<String> oSchemaNameFk = Optional.ofNullable(rs.getString("FKTABLE_SCHEM"));
        final String tableNameFk = rs.getString("FKTABLE_NAME");
        final String columNameFk = rs.getString("FKCOLUMN_NAME");

        final int keySeq = rs.getInt("KEY_SEQ");
        final int updateRule = rs.getInt("UPDATE_RULE");
        final int deleteRule = rs.getInt("DELETE_RULE");
        final String fkName = rs.getString("FK_NAME");
        final Optional<String> pkName = Optional.ofNullable(rs.getString("PK_NAME"));
        final int deferrability = rs.getInt("DEFERRABILITY");

        // PK
        Optional<CatalogReference> oCatRefPk = oCatalogNamePK.map(CatalogReferenceR::new);
        Optional<SchemaReference> oSchemaRefPk = oSchemaNamePk.map(sn -> new SchemaReferenceR(oCatRefPk, sn));
        TableReference tableReferencePk = new TableReferenceR(oSchemaRefPk, tableNamePk);
        ColumnReference primaryKeyColumn = new ColumnReferenceR(Optional.of(tableReferencePk), columNamePk);

        // FK
        Optional<CatalogReference> oCatRefFk = oCatalogNameFK.map(CatalogReferenceR::new);
        Optional<SchemaReference> oSchemaRefFk = oSchemaNameFk.map(sn -> new SchemaReferenceR(oCatRefFk, sn));
        TableReference tableReferenceFk = new TableReferenceR(oSchemaRefFk, tableNameFk);
        ColumnReference foreignKeyColumn = new ColumnReferenceR(Optional.of(tableReferenceFk), columNameFk);

        // Use FK_NAME from database if available, otherwise generate one
        String constraintName;
        if (fkName != null && !fkName.isBlank()) {
            constraintName = fkName;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("fk_").append(tableReferenceFk.name()).append("_").append(foreignKeyColumn.name())
                    .append("_").append(tableReferencePk.name()).append("_").append(primaryKeyColumn.name());
            constraintName = sb.toString();
        }

        return new ImportedKeyR(
                primaryKeyColumn,
                foreignKeyColumn,
                constraintName,
                keySeq,
                ImportedKey.ReferentialAction.of(updateRule),
                ImportedKey.ReferentialAction.of(deleteRule),
                pkName,
                ImportedKey.Deferrability.of(deferrability));
    }

}
