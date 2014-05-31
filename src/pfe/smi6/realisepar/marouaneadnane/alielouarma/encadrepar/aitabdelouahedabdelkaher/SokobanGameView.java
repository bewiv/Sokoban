package pfe.smi6.realisepar.marouaneadnane.alielouarma.encadrepar.aitabdelouahedabdelkaher;

import pfe.smi6.realisepar.marouaneadnane.alielouarma.encadrepar.aitabdelouahedabdelkaher.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class SokobanGameView extends View {

	static class GameMetrics {
		boolean levelFitsOnScreen; // niveau correspond à l'écran
		int tileSize; // taille des carreaux 
	}

	private Bitmap chestnutOnFloorBitmap;
	private Bitmap chestnutOnTargetBitmap;
	private Bitmap floorBitmap;
	private Bitmap scratOnFloorBitmap;
	private Bitmap scratOnTargetBitmap;
	private Bitmap targetBitmap;
	private Bitmap wallBitmap;
	final SokobanGameState game;
	boolean ignoreDrag;
	GameMetrics metrics;
	private int offsetX;
	private int offsetY;
	private Bitmap outsideBitmap;
	private final boolean hapticFeedback;
	
	private Bitmap scrat_down ;
	private Bitmap scrat_down_on_target;
	private Bitmap scrat_left;
	private Bitmap scrat_left_on_target;
	private Bitmap scrat_right;
	private Bitmap scrat_right_on_target;
	private Bitmap scrat_up;
	private Bitmap scrat_up_on_target;
	
	
	public SokobanGameView(Context context, AttributeSet attributes) {
		super(context, attributes);

		hapticFeedback = getContext().getSharedPreferences(SokobanMenuActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
		.getBoolean(SokobanMenuActivity.HAPTIC_FEEDBACK_PREFS_NAME,
				SokobanMenuActivity.HAPTIC_FEEDBACK_DEFAULT_VALUE);

		this.game = ((SokobanGameActivity) context).gameState;

		setOnTouchListener(new OnTouchListener() {
			private int xOffset;
			private int xTouch;
			private int yOffset;
			private int yTouch;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					ignoreDrag = false;
					xTouch = (int) event.getX();
					yTouch = (int) event.getY();
					xOffset = 0;
					yOffset = 0;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// perhaps move to clicked tile? if not is moving?
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (ignoreDrag)
						return true;

					// System.out.println("MOVING: " + event.getX() + ", " + event.getY());
					xOffset += xTouch - (int) event.getX();
					yOffset += yTouch - (int) event.getY();

					int dx = 0, dy = 0;

					if (Math.abs(xOffset) >= Math.abs(yOffset)) {
						// perhaps move x?
						dx = (xOffset) / metrics.tileSize;
						if (dx != 0) {
							yOffset = 0; // <= since we move horizontally, reset vertical offset
							xOffset -= dx * metrics.tileSize;
						}
					} else {
						// perhaps move y?
						dy = (yOffset) / metrics.tileSize;
						if (dy != 0) {
							xOffset = 0; // <= since we move vertically, reset horizontal offset
							yOffset -= dy * metrics.tileSize;
						}
					}

					performMove(-dx, -dy);

					xTouch = (int) event.getX();
					yTouch = (int) event.getY();
				}
				return true;
			}
		});

		setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() != KeyEvent.ACTION_DOWN)
					return false;

				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_UP:
					performMove(0, -1);
					break;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					performMove(1, 0);
					break;
				case KeyEvent.KEYCODE_DPAD_DOWN:
					performMove(0, 1);
					break;
				case KeyEvent.KEYCODE_DPAD_LEFT:
					performMove(-1, 0);
					break;
				default:
					return false;
				}
				return true;
			}
		});
	}

	/** Called by our own activity. */
	public void backPressed() {
		if (game.performUndo()) {
			centerScreenOnPlayerIfNecessary();
			invalidate();
		} else if (game.undos.isEmpty()){
			((Activity) getContext()).finish();
		}
	}

	private void centerScreenOnPlayer() {
		int[] playerPos = game.getPlayerPosition();
		int centerX = playerPos[0] * metrics.tileSize + metrics.tileSize / 2;
		int centerY = playerPos[1] * metrics.tileSize + metrics.tileSize / 2;
		// // offset + width/2 = centerX =>
		offsetX = centerX - getWidth() / 2;
		offsetY = centerY - getHeight() / 2;

		offsetX = -offsetX;
		offsetY = -offsetY;
	}

	private void centerScreenOnPlayerIfNecessary() {
		if (metrics.levelFitsOnScreen) {
			return;
		}

		int[] playerPos = game.getPlayerPosition();
		int playerX = playerPos[0];
		int playerY = playerPos[1];

		int tileSize = metrics.tileSize;
		int tilesLeftOfPlayer = (playerX * tileSize + offsetX) / tileSize;
		int tilesRightOfPlayer = (getWidth() - playerX * tileSize - offsetX) / tileSize;
		int tilesAboveOfPlayer = (playerY * tileSize + offsetY) / tileSize;
		int tilesBelowOfPlayer = (getHeight() - playerY * tileSize - offsetY) / tileSize;

		final int THRESHOLD = 1;
		if (tilesLeftOfPlayer <= THRESHOLD || tilesRightOfPlayer <= THRESHOLD || tilesAboveOfPlayer <= THRESHOLD
				|| tilesBelowOfPlayer <= THRESHOLD) {
			centerScreenOnPlayer();
			ignoreDrag = true;
		}
	}

	private void computeMetrics() {
		metrics = new GameMetrics();
		metrics.tileSize = SokobanGameActivity.IMAGE_SIZE;
		// "-1" since the whole border tiles does not need to fit on screen:
		metrics.levelFitsOnScreen = ((game.getWidthInTiles() - 1) * metrics.tileSize <= getWidth() && (game
				.getHeightInTiles() - 1)
				* metrics.tileSize <= getHeight());
	}

	public void customSizeChanged() {
		computeMetrics();

		Resources resources = getResources();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		
		// get the image  serie ice age 1
		if(SokobanLevelMenuActivity.series==0){
			scrat_down = BitmapFactory.decodeResource(resources, R.drawable.scrat_down_1, options);
			scrat_down_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_down_on_target_1, options);
			scrat_left = BitmapFactory.decodeResource(resources, R.drawable.scrat__left_1, options);
			scrat_left_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_left_on_target_1, options);
			scrat_right = BitmapFactory.decodeResource(resources, R.drawable.scrat_right_1, options);
			scrat_right_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_right_on_target_1, options);
			scrat_up = BitmapFactory.decodeResource(resources, R.drawable.scrat_up_1, options);
			scrat_up_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_up_on_target_1, options);
			
			chestnutOnFloorBitmap = BitmapFactory.decodeResource(resources, R.drawable.acorns_on_floor_1, options);
			chestnutOnTargetBitmap = BitmapFactory.decodeResource(resources, R.drawable.acorns_on_target_1, options);
			floorBitmap = BitmapFactory.decodeResource(resources, R.drawable.floor, options);
			scratOnFloorBitmap = BitmapFactory.decodeResource(resources, R.drawable.scrat_right_1, options);
			scratOnTargetBitmap = BitmapFactory.decodeResource(resources, R.drawable.scrat_right_on_target_1, options);
			outsideBitmap = BitmapFactory.decodeResource(resources, R.drawable.outside, options);
			targetBitmap = BitmapFactory.decodeResource(resources, R.drawable.target_1, options);
			wallBitmap = BitmapFactory.decodeResource(resources, R.drawable.wall, options);
		}
		/* serie 2 */
		else if(SokobanLevelMenuActivity.series==1){
			scrat_down = BitmapFactory.decodeResource(resources, R.drawable.scrat_down, options);
			scrat_down_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_down_on_target, options);
			scrat_left = BitmapFactory.decodeResource(resources, R.drawable.scrat__left, options);
			scrat_left_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_left_on_target, options);
			scrat_right = BitmapFactory.decodeResource(resources, R.drawable.scrat_right, options);
			scrat_right_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_right_on_target, options);
			scrat_up = BitmapFactory.decodeResource(resources, R.drawable.scrat_up, options);
			scrat_up_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_up_on_target, options);
			
			chestnutOnFloorBitmap = BitmapFactory.decodeResource(resources, R.drawable.acorns_on_floor, options);
			chestnutOnTargetBitmap = BitmapFactory.decodeResource(resources, R.drawable.acorns_on_target, options);
			floorBitmap = BitmapFactory.decodeResource(resources, R.drawable.floor, options);
			scratOnFloorBitmap = BitmapFactory.decodeResource(resources, R.drawable.scrat_right, options);
			scratOnTargetBitmap = BitmapFactory.decodeResource(resources, R.drawable.scrat_right_on_target, options);
			outsideBitmap = BitmapFactory.decodeResource(resources, R.drawable.outside, options);
			targetBitmap = BitmapFactory.decodeResource(resources, R.drawable.target, options);
			wallBitmap = BitmapFactory.decodeResource(resources, R.drawable.wall_meltdown, options);
		}
		/* serie 3 */
		else if(SokobanLevelMenuActivity.series==2){
			scrat_down = BitmapFactory.decodeResource(resources, R.drawable.scrat_down, options);
			scrat_down_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_down_on_target, options);
			scrat_left = BitmapFactory.decodeResource(resources, R.drawable.scrat__left, options);
			scrat_left_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_left_on_target, options);
			scrat_right = BitmapFactory.decodeResource(resources, R.drawable.scrat_right, options);
			scrat_right_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_right_on_target, options);
			scrat_up = BitmapFactory.decodeResource(resources, R.drawable.scrat_up, options);
			scrat_up_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_up_on_target, options);
			
			chestnutOnFloorBitmap = BitmapFactory.decodeResource(resources, R.drawable.acorns_on_floor, options);
			chestnutOnTargetBitmap = BitmapFactory.decodeResource(resources, R.drawable.acorns_on_target, options);
			floorBitmap = BitmapFactory.decodeResource(resources, R.drawable.floor, options);
			scratOnFloorBitmap = BitmapFactory.decodeResource(resources, R.drawable.scrat_right, options);
			scratOnTargetBitmap = BitmapFactory.decodeResource(resources, R.drawable.scrat_right_on_target, options);
			outsideBitmap = BitmapFactory.decodeResource(resources, R.drawable.outside, options);
			targetBitmap = BitmapFactory.decodeResource(resources, R.drawable.target, options);
			wallBitmap = BitmapFactory.decodeResource(resources, R.drawable.wall_downofthedinosaurs, options);
		}
		/* serie 4 */
		else if(SokobanLevelMenuActivity.series==3){
			scrat_down = BitmapFactory.decodeResource(resources, R.drawable.scrat_down, options);
			scrat_down_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_down_on_target, options);
			scrat_left = BitmapFactory.decodeResource(resources, R.drawable.scrat__left, options);
			scrat_left_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_left_on_target, options);
			scrat_right = BitmapFactory.decodeResource(resources, R.drawable.scrat_right, options);
			scrat_right_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_right_on_target, options);
			scrat_up = BitmapFactory.decodeResource(resources, R.drawable.scrat_up, options);
			scrat_up_on_target = BitmapFactory.decodeResource(resources, R.drawable.scrat_up_on_target, options);
			
			chestnutOnFloorBitmap = BitmapFactory.decodeResource(resources, R.drawable.acorns_on_floor, options);
			chestnutOnTargetBitmap = BitmapFactory.decodeResource(resources, R.drawable.acorns_on_target, options);
			floorBitmap = BitmapFactory.decodeResource(resources, R.drawable.floor, options);
			scratOnFloorBitmap = BitmapFactory.decodeResource(resources, R.drawable.scrat_right, options);
			scratOnTargetBitmap = BitmapFactory.decodeResource(resources, R.drawable.scrat_right_on_target, options);
			outsideBitmap = BitmapFactory.decodeResource(resources, R.drawable.outside, options);
			targetBitmap = BitmapFactory.decodeResource(resources, R.drawable.target, options);
			wallBitmap = BitmapFactory.decodeResource(resources, R.drawable.wall_drift, options);
		}
		float scaleFactor = metrics.tileSize / 96.0f;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleFactor, scaleFactor);

		int imageSize = 96;
		// recreate the new Bitmap
		
		scrat_down = Bitmap.createBitmap(scrat_down, 0, 0, imageSize, imageSize, matrix, true);
		scrat_down_on_target = Bitmap.createBitmap(scrat_down_on_target, 0, 0, imageSize, imageSize, matrix, true);
		scrat_left = Bitmap.createBitmap(scrat_left, 0, 0, imageSize, imageSize, matrix, true);
		scrat_left_on_target = Bitmap.createBitmap(scrat_left_on_target, 0, 0, imageSize, imageSize, matrix, true);
		scrat_right = Bitmap.createBitmap(scrat_right, 0, 0, imageSize, imageSize, matrix, true);
		scrat_right_on_target = Bitmap.createBitmap(scrat_right_on_target, 0, 0, imageSize, imageSize, matrix, true);
		scrat_up = Bitmap.createBitmap(scrat_up, 0, 0, imageSize, imageSize, matrix, true);
		scrat_up_on_target = Bitmap.createBitmap(scrat_up_on_target, 0, 0, imageSize, imageSize, matrix, true);
		
		chestnutOnFloorBitmap = Bitmap.createBitmap(chestnutOnFloorBitmap, 0, 0, imageSize, imageSize, matrix, true);
		chestnutOnTargetBitmap = Bitmap.createBitmap(chestnutOnTargetBitmap, 0, 0, imageSize, imageSize, matrix, true);
		floorBitmap = Bitmap.createBitmap(floorBitmap, 0, 0, imageSize, imageSize, matrix, true);
		scratOnFloorBitmap = Bitmap.createBitmap(scratOnFloorBitmap, 0, 0, imageSize, imageSize, matrix, true);
		scratOnTargetBitmap = Bitmap.createBitmap(scratOnTargetBitmap, 0, 0, imageSize, imageSize, matrix, true);
		outsideBitmap = Bitmap.createBitmap(outsideBitmap, 0, 0, imageSize, imageSize, matrix, true);
		targetBitmap = Bitmap.createBitmap(targetBitmap, 0, 0, imageSize, imageSize, matrix, true);
		wallBitmap = Bitmap.createBitmap(wallBitmap, 0, 0, imageSize, imageSize, matrix, true);

		if (metrics.levelFitsOnScreen) {
			int w = game.getWidthInTiles() * metrics.tileSize;
			int h = game.getHeightInTiles() * metrics.tileSize;
			// 2*offsetX + w = getWidth() =>
			offsetX = (getWidth() - w) / 2;
			offsetY = (getHeight() - h) / 2;
		} else {
			centerScreenOnPlayer();
		}
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawColor(Color.rgb(89, 181, 246));
		canvas.setDensity(Bitmap.DENSITY_NONE);

		final int widthInTiles = game.getWidthInTiles();
		final int heightInTiles = game.getHeightInTiles();
		final int tileSize = metrics.tileSize;

		for (int x = 0; x < widthInTiles; x++) {
			for (int y = 0; y < heightInTiles; y++) {
				int left = offsetX + tileSize * x;
				int top = offsetY + tileSize * y;

				Bitmap tileBitmap;
				char c = game.getItemAt(x, y);
				switch (c) {
				case '\'':
					tileBitmap = outsideBitmap;
					break;
				case SokobanGameState.CHAR_WALL:
					tileBitmap = wallBitmap;
					break;
				case SokobanGameState.CHAR_FLOOR:
					tileBitmap = floorBitmap;
					break;
				case SokobanGameState.CHAR_ACORNS_ON_FLOOR:
					tileBitmap = chestnutOnFloorBitmap;
					break;
				case SokobanGameState.CHAR_ACORNS_ON_TARGET:
					tileBitmap = chestnutOnTargetBitmap;
					break;
				case SokobanGameState.CHAR_TARGET:
					tileBitmap = targetBitmap;
					break;
				 case SokobanGameState.SCRAT_DOWN:
					 tileBitmap = scrat_down;
					 break;
				 case SokobanGameState.SCRAT_DOWN_ON_TARGET:
					 tileBitmap = scrat_down_on_target;
					 break;
				 case SokobanGameState.SCRAT_LEFT:
					 tileBitmap = scrat_left;
					 break;
				 case SokobanGameState.SCRAT_LEFT_ON_TARGET:
					 tileBitmap = scrat_left_on_target;
					 break;
				 case SokobanGameState.SCRAT_RIGHT:
					 tileBitmap = scrat_right;
					 break;
				 case SokobanGameState.SCRAT_RIGHT_ON_TARGET:
					 tileBitmap = scrat_right_on_target;
					 break;
				 case SokobanGameState.SCRAT_UP:
					 tileBitmap = scrat_up;
					 break;
				 case SokobanGameState.SCRAT_UP_ON_TARGET:
					 tileBitmap = scrat_up_on_target;
					 break;
				 
				default:
					throw new IllegalArgumentException(String.format("Invalid character at (%d,%d): %c", x, y, c));
				}

				canvas.drawBitmap(tileBitmap, left, top, null);
			}
		}
	}

	void gameOver() {
		if (hapticFeedback) {
			Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(300);
		}
		invalidate();

		SharedPreferences prefs = getContext().getSharedPreferences(SokobanMenuActivity.SHARED_PREFS_NAME,
				Context.MODE_PRIVATE);
		final String maxLevelPrefName = SokobanLevelMenuActivity.getMaxLevelPrefName(game.currentLevelSet);
		int currentMaxLevel = prefs.getInt(maxLevelPrefName, 1);
		int newMaxLevel = game.getCurrentLevel() + 2; // zero based level from getCurrentLevel()
		String message = "Level already cleared - no new level unlocked!";
		boolean levelSetDone = false;
		if (newMaxLevel > currentMaxLevel) {
			if (newMaxLevel - 1 >= SokobanLevels.levelMaps.get(game.currentLevelSet).length) {
				newMaxLevel--;
				message = "You completed all levels!";
				levelSetDone = true;
			} else {
				Editor editor = prefs.edit();
				editor.putInt(maxLevelPrefName, newMaxLevel);
				editor.commit();
				message = "New level unlocked!";
			}
		}

		AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
		alert.setCancelable(false);
		alert.setMessage(message);
		alert.setTitle("Congratulations");
		final int levelDestination = newMaxLevel - 1; // newMaxLevel was one based
		final boolean levelSetDoneFinal = levelSetDone;
		alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((Activity) getContext()).finish();
				if (!levelSetDoneFinal) {
					Intent intent = new Intent();
					intent.putExtra(SokobanGameActivity.GAME_LEVEL_INTENT_EXTRA, levelDestination);
					intent.putExtra(SokobanGameActivity.GAME_LEVEL_SET_EXTRA, game.currentLevelSet);
					intent.setClass(getContext(), SokobanGameActivity.class);
					getContext().startActivity(intent);
				}
			}
		});
		alert.show();
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {
		super.onSizeChanged(width, height, oldw, oldh);
		customSizeChanged();
	}

	void performMove(int dx, int dy) {
		if (game.tryMove(dx, dy)) {
			if (hapticFeedback) {
				performHapticFeedback(HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
			}
			centerScreenOnPlayerIfNecessary();
			invalidate();

			if (game.isDone()) {
				gameOver();
			}
		}
	}
}
