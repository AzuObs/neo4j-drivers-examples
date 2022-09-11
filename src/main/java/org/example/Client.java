package org.example;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.SessionConfig.builder;
import static org.neo4j.driver.Values.parameters;

public class Client implements AutoCloseable {

    private final Driver driver;

    public Client(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() {
        driver.close();
    }

    private Result addCompany(final Transaction tx, final String name) {
        return tx.run("CREATE (:Company {name: $name})", parameters("name", name));
    }

    private Result addPerson(final Transaction tx, final String name) {
        return tx.run("CREATE (:Person {name: $name})", parameters("name", name));
    }

    private Result employ(final Transaction tx, final String person, final String company) {
        return tx.run(
            "MATCH (person:Person {name: $person_name}) " + "MATCH (company:Company {name: $company_name}) "
                + "CREATE (person)-[:WORKS_FOR]->(company)",
            parameters("person_name", person, "company_name", company));
    }

    private Result makeFriends(final Transaction tx, final String person1, final String person2) {
        return tx.run(
            "MATCH (a:Person {name: $person_1}) " + "MATCH (b:Person {name: $person_2}) "
                + "MERGE (a)-[:KNOWS]->(b)",
            parameters("person_1", person1, "person_2", person2));
    }

    private Result printFriends(final Transaction tx) {
        Result result = tx.run("MATCH (a)-[:KNOWS]->(b) RETURN a.name, b.name");
        while (result.hasNext()) {
            Record record = result.next();
            System.out.println(String.format("%s knows %s", record.get("a.name").asString(), record.get("b.name").toString()));
        }
        return result;
    }

    public void addEmployAndMakeFriends() {
        List<Bookmark> savedBookmarks = new ArrayList<>();

        try (Session session1 = driver.session(builder().withDefaultAccessMode(AccessMode.WRITE).build())) {
            session1.writeTransaction(tx -> addCompany(tx, "Wayne Enterprises").consume());
            session1.writeTransaction(tx -> addCompany(tx, "Wayne Enterprises").consume());
            session1.writeTransaction(tx -> employ(tx, "Alice", "Wayne Enterprises").consume());
            savedBookmarks.add(session1.lastBookmark());
        }

        try (Session session2 = driver.session(builder().withDefaultAccessMode(AccessMode.WRITE).build())) {
            session2.writeTransaction(tx -> addCompany(tx, "LexCorp").consume());
            session2.writeTransaction(tx -> addPerson(tx, "Bob").consume());
            session2.writeTransaction(tx -> employ(tx, "Bob", "LexCorp").consume());
            savedBookmarks.add(session2.lastBookmark());
        }
    }
}
