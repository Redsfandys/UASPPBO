package interfaces;

import model.Tile;

public interface Movable {
    boolean move(int dRow, int dCol, Tile[][] maze);
}