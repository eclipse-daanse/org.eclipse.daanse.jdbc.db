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
package org.eclipse.daanse.jdbc.db.dialect.db.configurable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eclipse.daanse.jdbc.db.dialect.api.generator.BitOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ConfigurableDialectTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        void testBuilder_DefaultValues() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();

            assertThat(dialect.getQuoteIdentifierString()).isEqualTo("\"");
            assertThat(dialect.getDialectName()).isEqualTo("configurable");
            assertThat(dialect.allowsAs()).isTrue();
            assertThat(dialect.allowsFromQuery()).isTrue();
            assertThat(dialect.allowsJoinOn()).isTrue();
            assertThat(dialect.allowsCountDistinct()).isTrue();
            assertThat(dialect.supportsGroupByExpressions()).isTrue();
            assertThat(dialect.supportsGroupingSets()).isFalse();
        }

        @Test
        void testBuilder_CustomQuoteString() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().quoteIdentifierString("`").build();

            assertThat(dialect.getQuoteIdentifierString()).isEqualTo("`");
        }

        @Test
        void testBuilder_CustomDialectName() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().dialectName("custom-db").build();

            assertThat(dialect.getDialectName()).isEqualTo("custom-db");
        }

        @Test
        void testBuilder_MySqlStyle() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().quoteIdentifierString("`").dialectName("mysql")
                    .allowsAs(true).supportsGroupingSets(false).build();

            assertThat(dialect.getQuoteIdentifierString()).isEqualTo("`");
            assertThat(dialect.getDialectName()).isEqualTo("mysql");
        }

        @Test
        void testBuilder_SqlServerStyle() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().quoteIdentifierString("[")
                    .dialectName("sqlserver").allowsAs(true).build();

            assertThat(dialect.getQuoteIdentifierString()).isEqualTo("[");
            assertThat(dialect.getDialectName()).isEqualTo("sqlserver");
        }
    }

    @Nested
    @DisplayName("Identifier Quoting Tests")
    class IdentifierQuotingTests {

        @Test
        void testQuoteIdentifier_SingleName() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();

            StringBuilder result = dialect.quoteIdentifier("columnName");

            assertThat(result.toString()).isEqualTo("\"columnName\"");
        }

        @Test
        void testQuoteIdentifier_SingleName_Backticks() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().quoteIdentifierString("`").build();

            StringBuilder result = dialect.quoteIdentifier("columnName");

            assertThat(result.toString()).isEqualTo("`columnName`");
        }

        @Test
        void testQuoteIdentifier_QualifiedName() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();

            String result = dialect.quoteIdentifier("tableName", "columnName");

            assertThat(result).isEqualTo("\"tableName\".\"columnName\"");
        }

        @Test
        void testQuoteIdentifier_StringBuilder() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();
            StringBuilder buf = new StringBuilder();

            dialect.quoteIdentifier(buf, "schema", "table", "column");

            assertThat(buf.toString()).isEqualTo("\"schema\".\"table\".\"column\"");
        }

        @Test
        void testQuoteIdentifier_WithNullParts() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();
            StringBuilder buf = new StringBuilder();

            dialect.quoteIdentifier(buf, null, "table", "column");

            assertThat(buf.toString()).isEqualTo("\"table\".\"column\"");
        }

        @Test
        void testQuoteIdentifier_AlreadyQuoted() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();
            StringBuilder buf = new StringBuilder();

            dialect.quoteIdentifier("\"alreadyQuoted\"", buf);

            // Should not double-quote
            assertThat(buf.toString()).isEqualTo("\"alreadyQuoted\"");
        }

        @Test
        void testQuoteIdentifier_EscapeQuoteChar() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();
            StringBuilder buf = new StringBuilder();

            dialect.quoteIdentifier("name\"with\"quotes", buf);

            // Embedded quotes should be escaped
            assertThat(buf.toString()).isEqualTo("\"name\"\"with\"\"quotes\"");
        }
    }

    @Nested
    @DisplayName("Literal Quoting Tests")
    class LiteralQuotingTests {

        @Test
        void testQuoteStringLiteral() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();
            StringBuilder buf = new StringBuilder();

            dialect.quoteStringLiteral(buf, "hello");

            assertThat(buf.toString()).isEqualTo("'hello'");
        }

        @Test
        void testQuoteStringLiteral_WithSingleQuote() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();
            StringBuilder buf = new StringBuilder();

            dialect.quoteStringLiteral(buf, "it's");

            assertThat(buf.toString()).isEqualTo("'it''s'");
        }

        @Test
        void testQuoteDateLiteral() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();
            StringBuilder buf = new StringBuilder();

            dialect.quoteDateLiteral(buf, "2023-12-15");

            assertThat(buf.toString()).isEqualTo("DATE '2023-12-15'");
        }

        @Test
        void testQuoteTimeLiteral() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();
            StringBuilder buf = new StringBuilder();

            dialect.quoteTimeLiteral(buf, "14:30:00");

            assertThat(buf.toString()).isEqualTo("TIME '14:30:00'");
        }

        @Test
        void testQuoteTimestampLiteral() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();
            StringBuilder buf = new StringBuilder();

            dialect.quoteTimestampLiteral(buf, "2023-12-15 14:30:00");

            assertThat(buf.toString()).isEqualTo("TIMESTAMP '2023-12-15 14:30:00'");
        }

        @Test
        void testQuoteNumericLiteral() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();
            StringBuilder buf = new StringBuilder();

            dialect.quoteNumericLiteral(buf, "12345");

            assertThat(buf.toString()).isEqualTo("12345");
        }

        @Test
        void testQuoteBooleanLiteral_True() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();
            StringBuilder buf = new StringBuilder();

            dialect.quoteBooleanLiteral(buf, "TRUE");

            assertThat(buf.toString()).isEqualTo("TRUE");
        }

        @Test
        void testQuoteBooleanLiteral_Invalid() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();
            StringBuilder buf = new StringBuilder();

            assertThatThrownBy(() -> dialect.quoteBooleanLiteral(buf, "INVALID"))
                    .isInstanceOf(NumberFormatException.class);
        }
    }

    @Nested
    @DisplayName("SQL Function Wrapper Tests")
    class SqlFunctionWrapperTests {

        @Test
        void testWrapIntoSqlUpperCaseFunction() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();

            StringBuilder result = dialect.wrapIntoSqlUpperCaseFunction("column1");

            assertThat(result.toString()).isEqualTo("UPPER(column1)");
        }

        @Test
        void testWrapIntoSqlIfThenElseFunction() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();

            StringBuilder result = dialect.wrapIntoSqlIfThenElseFunction("cond", "then_val", "else_val");

            assertThat(result.toString()).isEqualTo("CASE WHEN cond THEN then_val ELSE else_val END");
        }
    }

    @Nested
    @DisplayName("DDL Tests")
    class DdlTests {

        @Test
        void testClearTable() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();

            String result = dialect.clearTable("schema", "table");

            assertThat(result).isEqualTo("TRUNCATE TABLE \"schema\".\"table\"");
        }

        @Test
        void testDropTable() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();

            String result = dialect.dropTable("schema", "table", false);

            assertThat(result).isEqualTo("DROP TABLE \"schema\".\"table\"");
        }

        @Test
        void testDropTable_IfExists() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();

            String result = dialect.dropTable("schema", "table", true);

            assertThat(result).isEqualTo("DROP TABLE IF EXISTS \"schema\".\"table\"");
        }

        @Test
        void testCreateSchema() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();

            String result = dialect.createSchema("myschema", false);

            assertThat(result).isEqualTo("CREATE SCHEMA \"myschema\"");
        }

        @Test
        void testCreateSchema_IfNotExists() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().build();

            String result = dialect.createSchema("myschema", true);

            assertThat(result).isEqualTo("CREATE SCHEMA IF NOT EXISTS \"myschema\"");
        }
    }

    @Nested
    @DisplayName("Feature Flag Tests")
    class FeatureFlagTests {

        @Test
        void testFeatureFlags() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().allowsAs(false).allowsFromQuery(false)
                    .requiresAliasForFromQuery(true).allowsJoinOn(false).supportsGroupingSets(true)
                    .requiresGroupByAlias(true).maxColumnNameLength(64).build();

            assertThat(dialect.allowsAs()).isFalse();
            assertThat(dialect.allowsFromQuery()).isFalse();
            assertThat(dialect.requiresAliasForFromQuery()).isTrue();
            assertThat(dialect.allowsJoinOn()).isFalse();
            assertThat(dialect.supportsGroupingSets()).isTrue();
            assertThat(dialect.requiresGroupByAlias()).isTrue();
            assertThat(dialect.getMaxColumnNameLength()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("Aggregation Function Tests")
    class AggregationFunctionTests {

        @Test
        void testBitAggregations_NotSupported() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().supportsBitAndAgg(false).supportsBitOrAgg(false)
                    .build();

            assertThatThrownBy(() -> dialect.generateBitAggregation(BitOperation.AND, "col"))
                    .isInstanceOf(UnsupportedOperationException.class);

            assertThatThrownBy(() -> dialect.generateBitAggregation(BitOperation.OR, "col"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void testBitAggregations_Supported() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().supportsBitAndAgg(true).supportsBitOrAgg(true)
                    .supportsBitXorAgg(true).build();

            assertThat(dialect.generateBitAggregation(BitOperation.AND, "col").toString()).isEqualTo("BIT_AND(col)");

            assertThat(dialect.generateBitAggregation(BitOperation.OR, "col").toString()).isEqualTo("BIT_OR(col)");

            assertThat(dialect.generateBitAggregation(BitOperation.XOR, "col").toString()).isEqualTo("BIT_XOR(col)");
        }

        @Test
        void testPercentile_NotSupported() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().supportsPercentileCont(false)
                    .supportsPercentileDisc(false).build();

            assertThatThrownBy(() -> dialect.generatePercentileCont(0.5, false, null, "col"))
                    .isInstanceOf(UnsupportedOperationException.class);

            assertThatThrownBy(() -> dialect.generatePercentileDisc(0.5, false, null, "col"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void testPercentile_Supported() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().supportsPercentileCont(true)
                    .supportsPercentileDisc(true).build();

            String contResult = dialect.generatePercentileCont(0.5, false, null, "col").toString();
            assertThat(contResult).contains("PERCENTILE_CONT(0.5)");
            assertThat(contResult).contains("ORDER BY");
            assertThat(contResult).contains("\"col\"");

            String discResult = dialect.generatePercentileDisc(0.5, true, "tbl", "col").toString();
            assertThat(discResult).contains("PERCENTILE_DISC(0.5)");
            assertThat(discResult).contains("DESC");
        }

        @Test
        void testListAgg_NotSupported() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().supportsListAgg(false).build();

            assertThatThrownBy(() -> dialect.generateListAgg("col", false, ",", null, null, null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void testListAgg_Supported() {
            ConfigurableDialect dialect = ConfigurableDialect.builder().supportsListAgg(true).build();

            String result = dialect.generateListAgg("col", true, ";", null, null, null).toString();
            assertThat(result).contains("LISTAGG(");
            assertThat(result).contains("DISTINCT");
            assertThat(result).contains("';'");
        }
    }
}
