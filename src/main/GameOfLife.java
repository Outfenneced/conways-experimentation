package main;

import java.awt.Rectangle;

public class GameOfLife {

    public static void main(String[] args) {
        GameOfLife gameOfLife = new GameOfLife(1, new Rectangle(200, 200));
        gameOfLife.start();
    }

    private int cellSize;
    private final Rectangle boardSize;
    private final GameOfLifeBoard board;

    private GameOfLife(int cellSize, Rectangle boardSize) {
        this.cellSize = cellSize;
        this.boardSize = boardSize;
        this.board = new GameOfLifeBoard(boardSize);
    }

    public void start() {

    }

}
