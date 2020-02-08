package main;

import java.awt.*;
import java.util.function.Function;

public class GameOfLifeBoard {
    private final Rectangle boardDimensions;
    private int[][] positions;
    private int[][] buffer;

    public GameOfLifeBoard(Rectangle boardDimensions) {
        this.boardDimensions = boardDimensions;
        this.positions = new int[boardDimensions.width][boardDimensions.height];
        resetBuffer();
    }

    public int[][] getPositions() {
        return positions.clone();
    }

    public void swapBuffer() {
        positions = buffer;
        resetBuffer();
    }

    public int getBufferValue(int x, int y) {
        return buffer[x][y];
    }

    public void setBufferValue(int x, int y, int value) {
        buffer[x][y] = value;
    }

    public void resetBuffer() {
        buffer = new int[boardDimensions.width][boardDimensions.height];
    }

    public void applyFunctionToBuffer(Function<Integer, Integer> function) {
        for (int x = 0; x < boardDimensions.width; x++) {
            for (int y = 0; y < boardDimensions.height; y++) {
                buffer[x][y] = function.apply(positions[x][y]);
            }
        }
    }
}
