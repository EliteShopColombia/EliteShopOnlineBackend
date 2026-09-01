package com.eliteshop.colombia.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LiquibaseMigrationTest {

  @Autowired private DataSource dataSource;

  @Test
  void shouldStartWhenTestProfile() throws Exception {
    assertThat(dataSource).isNotNull();
    try (Connection conn = dataSource.getConnection()) {
      assertThat(conn.isValid(5)).isTrue();
    }
  }

  @Test
  void shouldHaveCoreTablesWhenApplicationStarts() throws Exception {
    Set<String> tables = fetchTableNames();

    assertThat(tables).contains("customer");
    assertThat(tables).contains("seller");
    assertThat(tables).contains("product");
    assertThat(tables).contains("orders");
    assertThat(tables).contains("order_item");
    assertThat(tables).contains("payment_info");
    assertThat(tables).contains("cart");
    assertThat(tables).contains("cart_item");
  }

  @Test
  void shouldExistWhenChangelogMasterFile() throws Exception {
    ClassPathResource resource = new ClassPathResource("db/changelog/db.changelog-master.yaml");
    assertThat(resource.exists()).isTrue();
  }

  @Test
  void shouldExistWhenAllMigrationFiles() throws Exception {
    String[] expectedMigrations = {
      "db/migrations/001-create-initial-tables.yaml",
      "db/migrations/002-create-location-tables.yaml",
      "db/migrations/003-seller-update-at-nullable.yaml",
      "db/migrations/004-create-slack-message-queue.yaml",
      "db/migrations/005-create-seller-verification.yaml",
      "db/migrations/006-add-seller-is-verified.yaml",
      "db/migrations/007-seller-verification-delete-cascade.yaml",
      "db/migrations/009-create-product-review-image-tables.yaml",
      "db/migrations/012-create-cart-orderitem-payment-method.yaml",
      "db/migrations/013-add-epayco-customer-id-to-customer-payment-method.yaml",
      "db/migrations/016-add-shipping-fields-to-orders.yaml",
      "db/migrations/017-create-tracking-events.yaml",
      "db/migrations/018-add-dispute-reason-to-orders.yaml",
      "db/migrations/019-fix-payment-info-schema.yaml",
      "db/migrations/020-fix-constraints-and-indexes.yaml",
      "db/migrations/021-cleanup-and-extensions.yaml",
      "db/migrations/022-add-seller-profile-image.yaml",
    };

    for (String migration : expectedMigrations) {
      ClassPathResource resource = new ClassPathResource(migration);
      assertThat(resource.exists()).as("Migration file should exist: %s", migration).isTrue();
    }
  }

  private Set<String> fetchTableNames() throws Exception {
    Set<String> tables = new HashSet<>();
    try (Connection conn = dataSource.getConnection()) {
      DatabaseMetaData meta = conn.getMetaData();
      ResultSet rs = meta.getTables(null, null, "%", new String[] {"TABLE"});
      while (rs.next()) {
        tables.add(rs.getString("TABLE_NAME").toLowerCase());
      }
    }
    return tables;
  }
}
