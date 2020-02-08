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

public class GameOfLifeNormal {
	private final static int SIZE = 1;
	private final static int SIZEX = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth()/SIZE;
	private final static int SIZEY = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()/SIZE;

	private static JFrame frame = new JFrame();
	private static Graphics g;
	private static Timer timer = new Timer();
	private static DrawPointer dp = new DrawPointer(SIZEX/2, SIZEY/2);

	private static int[][] positions = new int[SIZEX][SIZEY]; //Set the field
	private static boolean pause = false;
	private static ArrayList<Integer> pressed = new ArrayList<>();

	private static boolean shouldPauseDraw = false;

	/* 
	 * TODO Further optimization of calculateAndDraw()
	 */


	public static void main(String[] args) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(SIZEX*SIZE,SIZEY*SIZE);
		frame.setLayout(null);
		frame.setVisible(true);
		g = frame.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, SIZEX*SIZE, SIZEY*SIZE);

		addListeners();
		posReset();

		timer.schedule(new UpdateTask(), 50L, 50L); //Might be better than at fixed rate (Maybe)
	}

	private static void update () {
		for(int key: pressed){
			keyCheck((char) key);
		}
		pressed.clear();

		BufferedImage i = null;

		if (!pause) {
			long startTime = System.nanoTime();
			i = calculateAndDraw(); //Calculate positions in the matrix when unpaused
			long endTime = System.nanoTime();
			System.out.println("CalculateAndDraw Time Per Square (Nano): " + (endTime-startTime)/(SIZEX*SIZEY));
		} else {
			if(shouldPauseDraw){
				long startTime = System.nanoTime();
				i = draw();
				long endTime = System.nanoTime();
				System.out.println("Draw Time Per Square (Nano): " + (endTime-startTime)/(SIZEX*SIZEY));
				shouldPauseDraw = false;
			}
		}

		if(i != null){
			g.drawImage(i, 0, 0, null); //Overwrite the previous with the new image setup
		}
	}

	private static BufferedImage calculateAndDraw(){
		BufferedImage img = new BufferedImage(SIZEX*SIZE, SIZEY*SIZE, BufferedImage.TYPE_3BYTE_BGR);
		Graphics bufImg = img.createGraphics();

		int[][] buffer = new int[SIZEX][SIZEY]; //Buffer of the field to ensure consistency as array changes
		for (int x = 1; x < SIZEX-1; x++){ //Iterate through horizontal
			for (int y = 1; y < SIZEY-1; y++){ //Iterate through vertical
				//Number of cells living near a specific cell
				int live = enumerateSurroundingLiving(x, y);

				if (positions[x][y] == 0) { //If cell being checked is dead
					if (live == 3) {
						buffer[x][y] = 1; //Change cell to living
						bufImg.setColor(Color.RED);
						bufImg.fillRect(x*SIZE, y*SIZE, SIZE, SIZE);
					}
				} else {
					if (live == 2 || live == 3) {
						if (positions[x][y] > 0) {
							buffer[x][y] = (short) (positions[x][y] + 1); //Increment number of generations a cell has lived
							bufImg.setColor(Color.getHSBColor(buffer[x][y] / 40f, 1.0f, 1.0f));
							if (positions[x][y] > 40) { //If cell was drawing pointer
								buffer[x][y] = 100; //Set to living
								bufImg.setColor(Color.WHITE);
							}
							bufImg.fillRect(x * SIZE, y * SIZE, SIZE, SIZE);
						}
					} else {
						if (positions[x][y] > 0) {
							buffer[x][y] = 0; //Change cell to dead
							bufImg.setColor(Color.BLACK);
							bufImg.fillRect(x*SIZE, y*SIZE, SIZE, SIZE);
						}
					}
				}
			}
		}
		positions = buffer.clone(); //Copy over the buffer back to the main field
		return img;
	}

	private static int enumerateSurroundingLiving(int x, int y) {
		int live = 0;
		for(int j = -1; j <= 1; j++){
			for(int i = -1; i <= 1; i++){
				if(!(j == i && i == 0)){ //Don't check the cell itself
					if(positions[x+i][y+j] > 0){ //Check if living
						live++;
					}
				}
			}
		}
		return live;
	}

	private static BufferedImage draw(){
		BufferedImage i = new BufferedImage(SIZEX*SIZE, SIZEY*SIZE, BufferedImage.TYPE_3BYTE_BGR); //Workaround to avoid graphics syncing due to address passing
		Graphics buf = i.createGraphics();//Workaround to avoid graphics syncing due to address passing

		for(int x = 0; x<SIZEX; x++){
			for(int y = 0; y<SIZEY; y++){
				int block = positions[x][y];
				if(block == 100){ //If it's either stable or the draw pointer, make white
					buf.setColor(Color.WHITE);
					buf.fillRect(x * SIZE, y * SIZE, SIZE, SIZE);
				} else if(block != 0){ //If occupied, change to color based on number of generations alive
					buf.setColor(Color.getHSBColor(block/40f, 1.0f, 1.0f));
					buf.fillRect(x * SIZE, y * SIZE, SIZE, SIZE);
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
	private static void posReset(){
		int x;
		int y;

		for (x=0; x<SIZEX;x++){ //Iterate through horizontal
			for (y=0; y<SIZEY;y++){ //Iterate through vertical
				positions[x][y] = 0;
			}
		}
		shouldPauseDraw = true;
	}

	//Randomizes the cells in the whole field
	private static void randomize(){
		short r;
		Random rand = new Random();
		for (int x = 0; x<SIZEX;x++){ //Iterate through horizontal
			for (int y = 0; y<SIZEY;y++){ //Iterate through vertical
				r = (short) rand.nextInt(2);
				positions[x][y] = r;
			}
		}
		shouldPauseDraw = true;
	}

	private static void invert() {
		for (int x = 0; x<SIZEX;x++){ //Iterate through horizontal
			for (int y = 0; y<SIZEY;y++){ //Iterate through vertical
				if(positions[x][y] == 0){
					positions[x][y] = 100;
				} else {
					positions[x][y] = 0;
				}
			}
		}
		shouldPauseDraw = true;
	}

	//Pauses running and activates drawing pointer
	private static void paused(){
		pause = true;
		int[] pos = dp.getPos();
		dp.setStoreUnder(positions[pos[0]][pos[1]]);
		positions[dp.getPos()[0]][dp.getPos()[1]] = 100;
		shouldPauseDraw = true;
	}

	//Unpauses running and deactivates drawing pointer
	private static void unpaused(){
		pause = false;
		int[] pos = dp.getPos();
		positions[pos[0]][pos[1]] = dp.getStoreUnder();
	}

	//Moves the drawing pointer
	private static void pointerMove(int dir){
		if(!pause){
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
	private static void keyCheck(char key){
		switch (key){
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
				if(e.getButton() == 1){
					positions[e.getX()/SIZE][e.getY()/SIZE] = 100;
				} else {
					positions[e.getX()/SIZE][e.getY()/SIZE] = 0;
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
				if(e.getButton() == 1){
					positions[e.getX()/SIZE][e.getY()/SIZE] = 100;
				} else {
					positions[e.getX()/SIZE][e.getY()/SIZE] = 0;
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