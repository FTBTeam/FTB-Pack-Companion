package dev.ftb.packcompanion.features;

public abstract class CommonFeature {
    public void setup() {
        if (isDisabled()) {
            return;
        }

        initialize();
    }

    public abstract void initialize();

    public boolean isDisabled() {
        return true;
    }
}
