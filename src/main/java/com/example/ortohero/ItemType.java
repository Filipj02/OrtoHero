package com.example.ortohero;

public enum ItemType {
    SWORD("Miecz", "Zmniejsza liczbę słów do wpisania do 2."),
    WAND("Różdżka", "Podmienia trudne słowo na łatwiejsze."),
    ARMOR("Zbroja", "Chroni przed utratą życia w tym zadaniu."),
    POTION("Mikstura zdrowia", "Przywraca 1 życie (max 3)."),
    FEATHER("Pióro Mądrości", "Odsłania jedną literę w wybranym słowie.");

    private final String displayName;
    private final String description;

    ItemType(String displayName, String description) {
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
