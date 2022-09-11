package org.example;

public class Main {
    public static void main(String... args) {
        try (BookmarkingExample client = new BookmarkingExample("bolt://localhost:7687", "neo4j", "neo")) {
            client.addEmployAndMakeFriends();
        }

        try (TransactionStateExample client = new TransactionStateExample("bolt://localhost:7687", "neo4j", "neo")) {
            client.addEmployAndMakeFriends();
        }
    }
}
