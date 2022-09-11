package org.example;

import org.neo4j.driver.*;

import static org.neo4j.driver.SessionConfig.builder;
import static org.neo4j.driver.Values.parameters;

/**
 * This example shows whether write data is saved on the QueryContext or on the TransactionContext.
 */
public class TransactionStateExample implements AutoCloseable {

  private final Driver driver;

  public TransactionStateExample(String uri, String user, String password) {
    driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
  }

  @Override
  public void close() {
    driver.close();
  }

  private Result addCompany(final Transaction tx, final String name) {
    return tx.run("CREATE (:Company {name: $name})", parameters("name", name));
  }

  public void addEmployAndMakeFriends() {
    try (Session session = driver.session(builder().withDefaultAccessMode(AccessMode.WRITE).build())) {
      session.run("MATCH (n) DETACH DELETE n");
      var txState = session.writeTransaction(tx -> {
        tx.run("CREATE (:Foo)");
        return tx.run("MATCH (n: Foo) RETURN n").list();
      });

      session.run("MATCH (n) DETACH DELETE n");
      var queryState = session.writeTransaction(tx ->
          tx.run("CREATE (:Foo) WITH 1 AS x MATCH (n: Foo) RETURN n").list());

      assert txState.size() == 1;
      assert queryState.size() == 1;
    }
  }
}
