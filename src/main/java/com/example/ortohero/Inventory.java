package com.example.ortohero;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Inventory {
    private final EnumMap<ItemType, Integer> items = new EnumMap<>(ItemType.class);

    public Inventory() {
        for (ItemType type : ItemType.values()) {
            items.put(type, 0);
        }
    }

    public static Inventory starterInventory() {
        Inventory inventory = new Inventory();
        inventory.add(ItemType.POTION, 1);
        inventory.add(ItemType.FEATHER, 1);
        inventory.add(ItemType.ARMOR, 1);
        return inventory;
    }

    public Map<ItemType, Integer> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public int getQuantity(ItemType type) {
        return items.getOrDefault(type, 0);
    }

    public void add(ItemType type, int amount) {
        items.merge(type, Math.max(0, amount), Integer::sum);
    }

    public boolean consume(ItemType type) {
        int current = items.getOrDefault(type, 0);
        if (current > 0) {
            items.put(type, current - 1);
            return true;
        }
        return false;
    }

    public void clear() {
        items.replaceAll((type, qty) -> 0);
    }
}
