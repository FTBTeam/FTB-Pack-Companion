package dev.ftb.packcompanion.features;

public abstract class CommonFeature {
    public void setup() {
        if (!isEnabled()) {
            return;
        }

        initialize();
    }

    public abstract void initialize();

    public boolean isEnabled() {
        return false;
    }
}
