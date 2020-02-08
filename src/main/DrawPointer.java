package main;

public class DrawPointer {
    private int[] pos = {0, 0};
    private int storeUnder = 0;

    public DrawPointer(int x, int y) {
        pos[0] = x;
        pos[1] = y;
    }

    public int[] pointerMove(int dir) {
        switch (dir) {
            case 1:
                pos[1]++;
                break;
            case 2:
                pos[0]--;
                break;
            case 3:
                pos[1]--;
                break;
            case 4:
                pos[0]++;
                break;
        }
        return pos;
    }

    public int[] getPos() {
        return pos;
    }

    public void setPos(int[] pos) {
        this.pos = pos;
    }

    public int getStoreUnder() {
        return storeUnder;
    }

    public void setStoreUnder(int storeUnder) {
        this.storeUnder = storeUnder;
    }
}
