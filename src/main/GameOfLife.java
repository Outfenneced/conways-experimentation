package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

public class GameOfLife {
    private final static int SIZE = 2;
    private final static int SIZEX = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / SIZE;
    private final static int SIZEY = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / SIZE;
    private final static int MAX_WHITE = 1000;

    private static JFrame frame = new JFrame();
    private static Graphics g;
    private static Timer timer = new Timer();
    private static DrawPointer dp = new DrawPointer(SIZEX / 2, SIZEY / 2);

    private static int[][] positions = new int[SIZEX][SIZEY];
    private static boolean pause = false;
    private static ArrayList<Integer> pressed = new ArrayList<>();

    private static boolean shouldPauseDraw = false;

    /*
     * TODO Further optimization of calculateAndDraw()
     */

    public static void main(String[] args) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(SIZEX * SIZE, SIZEY * SIZE);
        frame.setLayout(null);
        frame.setVisible(true);
        g = frame.getGraphics();

        addListeners();
        posReset();

        timer.schedule(new UpdateTask(), 1L, 1L); //Might be better than at fixed rate (Maybe)
    }

    private static void update() {
        for (int key : pressed) {
            keyCheck((char) key);
        }
        pressed.clear();

        if (!pause) {
            g.drawImage(calculateAndDraw(), 0, 0, null);
        } else if (shouldPauseDraw) {
            g.drawImage(draw(), 0, 0, null);
            shouldPauseDraw = false;
        }
    }

    private static BufferedImage calculateAndDraw() {
        BufferedImage img = new BufferedImage(SIZEX * SIZE, SIZEY * SIZE, BufferedImage.TYPE_3BYTE_BGR);
        Graphics bufImg = img.createGraphics();

        int[][] buffer = new int[SIZEX][SIZEY];

        for (int x = 1; x < SIZEX - 1; x++) { //Iterate through horizontal
            for (int y = 1; y < SIZEY - 1; y++) { //Iterate through vertical
                //Number of cells living near a specific cell
                int live = enumerateSurroundingLiving(x, y);
                int cellValue = positions[x][y];

                if (cellValue > 0) { //Alive cell
                    if (live == 2 || live == 3) { //Stay alive
                        buffer[x][y] = cellValue + 1;
                        if (cellValue > MAX_WHITE) {
                            bufImg.setColor(Color.DARK_GRAY);
                        } else {
                            bufImg.setColor(Color.getHSBColor(buffer[x][y] / (float) MAX_WHITE, 1.0f - (buffer[x][y] / (float) (MAX_WHITE * 1.5)), 1.0f - (buffer[x][y] / (float) (MAX_WHITE * 1.5))));
                        }
                    } else {
                        buffer[x][y] = 0;
                        bufImg.setColor(new Color(128, 128, 128));
                    }
                } else { //Dead cell
                    if (live == 3) { //Become alive
                        buffer[x][y] = 1; //Change cell to living
                        bufImg.setColor(Color.RED);
                    } else { //Stay dead
                        buffer[x][y] = cellValue - 1;
                        int grey = 128 - (int) (255 * (buffer[x][y] * -1 / (float) MAX_WHITE * 2));
                        if (grey > 0) {
                            bufImg.setColor(new Color(grey, grey, grey));
                        } else {
                            //bufImg.setColor(Color.BLACK);
                        }
                    }
                }
                bufImg.fillRect(x * SIZE, y * SIZE, SIZE, SIZE);

            }
        }
        positions = buffer; //Copy over the buffer back to the main field
        return img;
    }

    private static int enumerateSurroundingLiving(int x, int y) {
        int live = 0;
        for (int j = -1; j <= 1; j++) {
            for (int i = -1; i <= 1; i++) {
                if (!(j == i && i == 0)) { //Don't check the cell itself
                    if (positions[x + i][y + j] > 0) { //Check if living
                        live++;
                    }
                }
            }
        }
        return live;
    }

    private static BufferedImage draw() {
        BufferedImage i = new BufferedImage(SIZEX * SIZE, SIZEY * SIZE, BufferedImage.TYPE_3BYTE_BGR); //Workaround to avoid graphics syncing due to address passing
        Graphics bufImg = i.createGraphics();//Workaround to avoid graphics syncing due to address passing

        for (int x = 0; x < SIZEX; x++) {
            for (int y = 0; y < SIZEY; y++) {
                int block = positions[x][y];

                if (block <= 0) {
                    int grey = 128 - (int) (255 * (block * -1 / (float) MAX_WHITE * 2));
                    if (grey > 0) {
                        bufImg.setColor(new Color(grey, grey, grey));
                        bufImg.fillRect(x * SIZE, y * SIZE, SIZE, SIZE);
                    }
                } else {
                    if (block > MAX_WHITE) {
                        bufImg.setColor(Color.DARK_GRAY);
                    } else {
                        bufImg.setColor(Color.getHSBColor(block / (float) MAX_WHITE, 1.0f - (block / (float) (MAX_WHITE * 1.5)), 1.0f - (block / (float) (MAX_WHITE * 1.5))));
                    }
                    bufImg.fillRect(x * SIZE, y * SIZE, SIZE, SIZE);
                }
            }
        }
        return i;
    }

    static class UpdateTask extends TimerTask {
        @Override
        public void run() {
            update();
            System.out.println("Time: " + (System.currentTimeMillis() - scheduledExecutionTime()));
        }
    }

    //Initializes the positions
    private static void posReset() {
        int x;
        int y;

        for (x = 0; x < SIZEX; x++) { //Iterate through horizontal
            for (y = 0; y < SIZEY; y++) { //Iterate through vertical
                positions[x][y] = 0;
            }
        }
        shouldPauseDraw = true;
    }

    //Randomizes the cells in the whole field
    private static void randomize() {
        Random rand = new Random();
        for (int x = 0; x < SIZEX; x++) { //Iterate through horizontal
            for (int y = 0; y < SIZEY; y++) { //Iterate through vertical
                positions[x][y] = rand.nextInt(2);
            }
        }
        shouldPauseDraw = true;
    }

    private static void invert() {
        for (int x = 0; x < SIZEX; x++) { //Iterate through horizontal
            for (int y = 0; y < SIZEY; y++) { //Iterate through vertical
                if (positions[x][y] == 0) {
                    positions[x][y] = 100;
                } else {
                    positions[x][y] = 0;
                }
            }
        }
        shouldPauseDraw = true;
    }

    //Pauses running and activates drawing pointer
    private static void paused() {
        pause = true;
        int[] pos = dp.getPos();
        dp.setStoreUnder(positions[pos[0]][pos[1]]);
        positions[dp.getPos()[0]][dp.getPos()[1]] = 100;
        shouldPauseDraw = true;
    }

    //Unpauses running and deactivates drawing pointer
    private static void unpaused() {
        pause = false;
        int[] pos = dp.getPos();
        positions[pos[0]][pos[1]] = dp.getStoreUnder();
    }

    //Moves the drawing pointer
    private static void pointerMove(int dir) {
        if (!pause) {
            return;
        }
        int[] prevPos = dp.getPos();
        positions[prevPos[0]][prevPos[1]] = dp.getStoreUnder();

        int[] pos = dp.pointerMove(dir);
        dp.setStoreUnder(positions[pos[0]][pos[1]]);
        positions[pos[0]][pos[1]] = 100;
        shouldPauseDraw = true;
    }

    //Analyzes the key presses for their functions
    private static void keyCheck(char key) {
        switch (key) {
            case 'P':
                if (!pause) {
                    paused();
                    System.out.println("paused");
                } else {
                    unpaused();
                    System.out.println("unpaused");
                }
                break;
            case 'R':
                randomize();
                break;
            case 'C':
                posReset();
                break;
            case 'I':
                invert();
                break;
            case 'S':
                pointerMove(1);
                break;
            case 'A':
                pointerMove(2);
                break;
            case 'W':
                pointerMove(3);
                break;
            case 'D':
                pointerMove(4);
                break;
            case '[':
                positions[dp.getPos()[0]][dp.getPos()[1]] = 100;
                dp.setStoreUnder((short) 100);
                shouldPauseDraw = true;
                break;
            case ']':
                positions[dp.getPos()[0]][dp.getPos()[1]] = 0;
                dp.setStoreUnder((short) 0);
                shouldPauseDraw = true;
                break;
        }
    }

    private static void addListeners() {
        frame.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                pressed.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }

            @Override
            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub
            }
        });

        frame.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 1) {
                    positions[e.getX() / SIZE][e.getY() / SIZE] = 100;
                } else {
                    positions[e.getX() / SIZE][e.getY() / SIZE] = 0;
                }
                shouldPauseDraw = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }

        });
        frame.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (e.getButton() == 1) {
                    positions[e.getX() / SIZE][e.getY() / SIZE] = 100;
                } else {
                    positions[e.getX() / SIZE][e.getY() / SIZE] = 0;
                }
                shouldPauseDraw = true;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // TODO Auto-generated method stub

            }

        });
    }
}