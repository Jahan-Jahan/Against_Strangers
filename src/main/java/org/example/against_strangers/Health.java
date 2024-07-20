package org.example.against_strangers;

public class Health {
    private int health;

    public Health(int health) {
        this.health = health;
    }

    public int getHealth() {
        return health;
    }

    public void decreaseHealth(int amount) {
        health -= amount;
    }

    public boolean isDead() {
        return health <= 0;
    }
}
