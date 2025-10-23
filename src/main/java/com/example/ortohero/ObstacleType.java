package com.example.ortohero;

public enum ObstacleType {
    CREATURE("Błędopis", "Pokonaj stwora wpisując poprawnie słowa."),
    BRIDGE("Zawalony most", "Napraw most dzięki poprawnej pisowni."),
    TREE("Przewalone drzewo", "Usuń przeszkodę rozwiązując zadanie."),
    GATE("Brama zamku", "Otwórz bramę zdobywając klucz wiedzy."),
    ALTAR("Ołtarz wiedzy", "Aktywuj ołtarz udowadniając swoje umiejętności.");

    private final String displayName;
    private final String description;

    ObstacleType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
