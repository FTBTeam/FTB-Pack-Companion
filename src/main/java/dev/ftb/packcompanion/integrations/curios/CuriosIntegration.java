package dev.ftb.packcompanion.integrations.curios;

import dev.ftb.packcompanion.integrations.InventorySearcher;

public class CuriosIntegration {
    public static void init() {
        InventorySearcher.INSTANCE.registerProvider(new CuriosInventorySearcher());
    }
}
