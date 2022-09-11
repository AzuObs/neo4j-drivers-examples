package org.example;

public class Main {
    public static void main(String... args) {
        try (Client client = new Client("bolt://localhost:7687", "neo4j", "neo")) {
            client.addEmployAndMakeFriends();
        }
    }
}