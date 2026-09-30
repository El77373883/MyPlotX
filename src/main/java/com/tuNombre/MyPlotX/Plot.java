package com.tuNombre.MyPlotX;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Plot {

    private final String id;
    private UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> denied = new HashSet<>();
    private boolean forSale = false;
    private double price = 0.0;
    private String name = "";

    public Plot(String id, UUID owner) {
        this.id = id;
        this.owner = owner;
    }

    public String getId() { return id; }
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }
    public Set<UUID> getMembers() { return members; }
    public Set<UUID> getDenied() { return denied; }
    public boolean isForSale() { return forSale; }
    public void setForSale(boolean forSale) { this.forSale = forSale; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isOwner(UUID uuid) { return owner != null && owner.equals(uuid); }
    public boolean isMember(UUID uuid) { return members.contains(uuid); }
    public boolean isDenied(UUID uuid) { return denied.contains(uuid); }
}
