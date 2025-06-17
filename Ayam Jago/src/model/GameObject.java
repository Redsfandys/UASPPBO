package model;

import java.awt.Graphics;

public abstract class GameObject {
    protected int row;
    protected int col;
    protected int size; // Size in pixels

    public GameObject(int row, int col, int size) {
        this.row = row;
        this.col = col;
        this.size = size;
    }

    // Getters and setters
    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getSize() {
        return size;
    }

    // Abstract methods to be implemented by subclasses
    public abstract void draw(Graphics g);
    public abstract void update();
}
