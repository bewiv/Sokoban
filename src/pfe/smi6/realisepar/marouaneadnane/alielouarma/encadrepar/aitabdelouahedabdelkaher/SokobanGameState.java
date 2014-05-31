package pfe.smi6.realisepar.marouaneadnane.alielouarma.encadrepar.aitabdelouahedabdelkaher;

import java.io.Serializable;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class SokobanGameState implements Serializable {

	static class Undo implements Serializable {

		public char c1;
		public char c2;
		public char c3;

		public byte x1;
		public byte x2;
		public byte x3;

		public byte y1;
		public byte y2;
		public byte y3;
	}

	public static final char CHAR_ACORNS_ON_FLOOR = '$';
	public static final char CHAR_ACORNS_ON_TARGET = '*';
	public static final char CHAR_FLOOR = ' ';
	public static final char CHAR_SCRAT_ON_TARGET = '+';
	public static final char CHAR_TARGET = '.';
	public static final char CHAR_WALL = '#';
	
	public static final char SCRAT_DOWN = '2';
	public static final char SCRAT_DOWN_ON_TARGET = '6';
	public static final char SCRAT_LEFT = '3';
	public static final char SCRAT_LEFT_ON_TARGET = '7';
	public static final char SCRAT_RIGHT = '4';
	public static final char SCRAT_RIGHT_ON_TARGET = '8';
	public static final char SCRAT_UP = '@';
	public static final char SCRAT_UP_ON_TARGET = '5';

	// new position of acorns
	private static char newCharWhenAcornsPushed(char current) {
		return (current == CHAR_FLOOR) ? CHAR_ACORNS_ON_FLOOR : CHAR_ACORNS_ON_TARGET;
	}
	// original position of acorns
	private static char newdown(char current) {
		switch (current) {
		case CHAR_FLOOR:
		case CHAR_ACORNS_ON_FLOOR:
			return SCRAT_DOWN;
		case CHAR_TARGET:
		case CHAR_ACORNS_ON_TARGET:
			return SCRAT_DOWN_ON_TARGET;
		}
		throw new RuntimeException("Invalid current char: '" + current + "'");
	}

	private static char newleft(char current) {
		switch (current) {
		case CHAR_FLOOR:
		case CHAR_ACORNS_ON_FLOOR:
			return SCRAT_LEFT;
		case CHAR_TARGET:
		case CHAR_ACORNS_ON_TARGET:
			return SCRAT_LEFT_ON_TARGET;
		}
		throw new RuntimeException("Invalid current char: '" + current + "'");
	}
	private static char newRIGHT(char current) {
		switch (current) {
		case CHAR_FLOOR:
		case CHAR_ACORNS_ON_FLOOR:
			return SCRAT_RIGHT;
		case CHAR_TARGET:
		case CHAR_ACORNS_ON_TARGET:
			return SCRAT_RIGHT_ON_TARGET;
		}
		throw new RuntimeException("Invalid current char: '" + current + "'");
	}
	private static char newUP(char current) {
		switch (current) {
		case CHAR_FLOOR:
		case CHAR_ACORNS_ON_FLOOR:
			return SCRAT_UP;
		case CHAR_TARGET:
		case CHAR_ACORNS_ON_TARGET:
			return SCRAT_UP_ON_TARGET;
		}
		throw new RuntimeException("Invalid current char: '" + current + "'");
	}
	// new position of scrat
	private static char originalCharWhenScratLeaves(char current) {
		return (current == SCRAT_DOWN || current == SCRAT_LEFT ||current == SCRAT_RIGHT ||current == SCRAT_UP) ? CHAR_FLOOR : CHAR_TARGET;
	}
	// table 2 dimension in type char( map) initializes a table of type string
	private static char[][] stringArrayToCharMatrix(String[] s) {
		char[][] result = new char[s[0].length()][s.length];
		for (int x = 0; x < s[0].length(); x++) {
			for (int y = 0; y < s.length; y++) {
				result[x][y] = s[y].charAt(x);
			}
		}
		return result;
	}

	private int currentLevel; // series
	final int currentLevelSet; // level
	private char[][] map;
	private transient final int[] playerPosition = new int[2]; // position of scrat
	final LinkedList<Undo> undos = new LinkedList<Undo>(); // list of object type undo

	// Constructor
	public SokobanGameState(int level, int levelSet) {
		currentLevel = level;
		currentLevelSet = levelSet;
		loadLevel(currentLevel, levelSet);
	}
	
	// get current level
	public int getCurrentLevel() {
		return currentLevel;
	}
	
	// get larger map
	public int getHeightInTiles() {
		return map[0].length;
	}
	
	// retourn char 
	public char getItemAt(int x, int y) {
		return map[x][y];
	}
	
	//return position of scrat
	public int[] getPlayerPosition() {
		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[0].length; y++) {
				char c = map[x][y];
				if (SCRAT_DOWN == c || SCRAT_DOWN_ON_TARGET == c || SCRAT_LEFT == c || SCRAT_LEFT_ON_TARGET == c ||SCRAT_RIGHT == c || SCRAT_RIGHT_ON_TARGET == c || SCRAT_UP == c || SCRAT_UP_ON_TARGET == c ) {
					playerPosition[0] = x;
					playerPosition[1] = y;
				}
			}
		}
		return playerPosition;
	}

	public int getWidthInTiles() {
		return map.length;
	}

	public boolean isDone() {
		for (int x = 0; x < map.length; x++)
			for (int y = 0; y < map[0].length; y++)
				if (map[x][y] == CHAR_ACORNS_ON_FLOOR)
					return false;
		return true;
	}

	private void loadLevel(int level, int levelSet) {
		this.currentLevel = level;
		map = stringArrayToCharMatrix(SokobanLevels.levelMaps.get(levelSet)[level]);
	}

	public boolean performUndo() {
		if (undos.isEmpty())
			return false;
		Undo undo = undos.removeLast();
		map[undo.x1][undo.y1] = undo.c1;
		map[undo.x2][undo.y2] = undo.c2;
		if (undo.c3 != 0)
			map[undo.x3][undo.y3] = undo.c3;
		return true;
	}

	public void restart() {
		loadLevel(currentLevel, currentLevelSet);
		undos.clear();
	}

	/** Return whether something was changed. */
	public boolean tryMove(int dx, int dy) {
		if (dx == 0 && dy == 0)
			return false;

		if (dx != 0 && dy != 0) {
			throw new IllegalArgumentException("Can only move straight lines. dx=" + dx + ", dy=" + dy);
		}

		int steps = Math.max(Math.abs(dx), Math.abs(dy));
		int stepX = (dx == 0) ? 0 : (int) Math.signum(dx);
		int stepY = (dy == 0) ? 0 : (int) Math.signum(dy);

		boolean somethingChanged = false;

		int playerX = -1;
		int playerY = -1;
		// find player position
		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[0].length; y++) {
				char c = map[x][y];
				if (SCRAT_DOWN == c || SCRAT_DOWN_ON_TARGET == c || SCRAT_LEFT == c || SCRAT_LEFT_ON_TARGET == c ||SCRAT_RIGHT == c || SCRAT_RIGHT_ON_TARGET == c || SCRAT_UP == c || SCRAT_UP_ON_TARGET == c ) {

					playerX = x;//position current of the player
					playerY = y;//position current of the player
				}
			}
		}

		for (int i = 0; i < steps; i++) {
			int newX = playerX + stepX;
			int newY = playerY + stepY;

			boolean ok = false;
			boolean pushed = false;

			switch (map[newX][newY]) {
			case CHAR_FLOOR:
				// move to empty space
			case CHAR_TARGET:
				// move to empty target
				ok = true;
				break;
			case CHAR_ACORNS_ON_FLOOR:
				// pushing away acorn on floor
			case CHAR_ACORNS_ON_TARGET:
				// pushing away acorn on target
				char pushTo = map[newX + stepX][newY + stepY];
				ok = (pushTo == CHAR_FLOOR || pushTo == CHAR_TARGET);
				// ok if pushing to empty space
				if (ok) {
					pushed = true;
				}
				break;
			}

			if (ok) {
				Undo undo;
				if (undos.size() > 2000) {
					// size of undo: 9 bytes + object overhead = 25?
					// reuse and clear undo object
					undo = undos.removeFirst();
					undo.c3 = 0;
				} else {
					undo = new Undo();
				}
				undos.add(undo);
				somethingChanged = true;

				if (pushed) {
					byte pushedX = (byte) (newX + stepX);
					byte pushedY = (byte) (newY + stepY);
					undo.x3 = pushedX;
					undo.y3 = pushedY;
					undo.c3 = map[pushedX][pushedY];
					map[pushedX][pushedY] = newCharWhenAcornsPushed(map[pushedX][pushedY]);
				}

				undo.x1 = (byte) playerX;
				undo.y1 = (byte) playerY;
				undo.c1 = map[playerX][playerY];
				map[playerX][playerY] = originalCharWhenScratLeaves(map[playerX][playerY]);
				undo.x2 = (byte) newX;
				undo.y2 = (byte) newY;
				undo.c2 = map[newX][newY];
				if(dx==0 && dy==-1){
					map[newX][newY] = newUP(map[newX][newY]);
				}
				if(dx==1 && dy==0){
					map[newX][newY] = newRIGHT(map[newX][newY]);
				}
				if(dx==0 && dy==1){
					map[newX][newY] = newdown(map[newX][newY]);
				}
				if(dx==-1 && dy==0){
					map[newX][newY] = newleft(map[newX][newY]);
				}

				playerX = newX;
				playerY = newY;
				if (isDone()) {
					// if moving multiple steps at once, stop if an intermediate step may finish the game:
					return true;
				}
			}
		}
		return somethingChanged;
	}

}
