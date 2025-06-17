package model.character;

public abstract class Character {
    protected int x;
    protected int y;
    
    // Path to image assets (will be set by the game)
    public static String IMAGE_BASE_PATH = "assets/";
    
    public Character(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }
}
