package yap.one;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class YapCrossDetRecursive {
	private static List<List<Integer>> LEFT;
	private static List<List<Integer>> UP;
	private static int N;
	private static int M;
	private static int[][] MATRIX;
	private static int[] freedomsLeft;
	private static int[] freedomsUp;

	private static final int d = 15;
	private static long startTimeTotal;
	private static long startTimeRecursive;
	private static int blackCount = 0;
	private static int whiteCount = 0;

	private static List<Integer> minIndexes;
	private static List<Integer> maxIndexes;

	private static List<List<Integer>> loadMatrix(String path) {
		List<List<Integer>> matrix = new ArrayList<List<Integer>>();
		try {
			byte[] fileContent = Files.readAllBytes(new File(path).toPath());
			String text = new String(fileContent);
			String[] lines = text.split("\n");
			for (String line : lines) {
				List<Integer> list = new ArrayList<Integer>();
				String[] parts = line.split(" ");
				for (String part : parts) {
					if (part.isEmpty()) {
						continue;
					}
					list.add(Integer.parseInt(part));
				}
				if (!list.isEmpty()) {
					matrix.add(list);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return matrix;
	}

	private static void setUpBorders() {
		for (int j = 0; j < M; ++j) {
			if (MATRIX[0][j] != 1) {
				continue;
			}
			int value = UP.get(j).get(0);
			for (int i = 1; i < value; ++i) {
				MATRIX[i][j] = 1;
			}
			if (value < N) {
				MATRIX[value][j] = -1;
			}
			if (UP.get(j).size() == 1) {
				for (int i = value + 1; i < N; ++i) {
					MATRIX[i][j] = -1;
				}
			}
		}
	}

	private static void setDownBorders() {
		for (int j = 0; j < M; ++j) {
			if (MATRIX[N - 1][j] != 1) {
				continue;
			}
			int value = UP.get(j).get(UP.get(j).size() - 1);
			for (int i = N - 2; i >= N - value; --i) {
				MATRIX[i][j] = 1;
			}
			if (N - value - 1 >= 0) {
				MATRIX[N - value - 1][j] = -1;
			}
			if (UP.get(j).size() == 1) {
				for (int i = 0; i < N - value - 1; ++i) {
					MATRIX[i][j] = -1;
				}
			}
		}
	}

	private static void setLeftBorders() {
		for (int i = 0; i < N; ++i) {
			if (MATRIX[i][0] != 1) {
				continue;
			}
			int value = LEFT.get(i).get(0);
			for (int j = 1; j < value; ++j) {
				MATRIX[i][j] = 1;
			}
			if (value < M) {
				MATRIX[i][value] = -1;
			}
			if (LEFT.get(i).size() == 1) {
				for (int j = value + 1; j < M; ++j) {
					MATRIX[i][j] = -1;
				}
			}
		}
	}

	private static void setRightBorders() {
		for (int i = 0; i < N; ++i) {
			if (MATRIX[i][M - 1] != 1) {
				continue;
			}
			int value = LEFT.get(i).get(LEFT.get(i).size() - 1);
			for (int j = M - 2; j >= M - value; --j) {
				MATRIX[i][j] = 1;
			}
			if (M - value - 1 >= 0) {
				MATRIX[i][M - value - 1] = -1;
			}
			if (LEFT.get(i).size() == 1) {
				for (int j = 0; j < M - value - 1; ++j) {
					MATRIX[i][j] = -1;
				}
			}
		}
	}

	private static void setOneGroupLeft() {
		for (int i = 0; i < N; ++i) {
			List<Integer> list = LEFT.get(i);
			if (list.size() > 1) {
				continue;
			}
			int startJ = -1;
			int endJ = -1;
			for (int j = 0; j < M; ++j) {
				if (MATRIX[i][j] == 1) {
					startJ = j;
					for (int j2 = j + 1; j2 < M; ++j2) {
						if (MATRIX[i][j2] != 1) {
							endJ = j2 - 1;
							break;
						}
					}
					endJ = endJ == -1 ? M - 1 : endJ;
					break;
				}
			}
			if (startJ == -1) {
				continue;
			}
			int value = list.get(0);
			int remain = value - (endJ - startJ + 1);
			int leftIndex = startJ - remain;
			for (int j = 0; j < leftIndex; ++j) {
				MATRIX[i][j] = -1;
			}
			int rightIndex = endJ + remain;
			for (int j = rightIndex + 1; j < M; ++j) {
				MATRIX[i][j] = -1;
			}
		}
	}

	private static void setOneGroupUp() {
		for (int j = 0; j < M; ++j) {
			List<Integer> list = UP.get(j);
			if (list.size() > 1) {
				continue;
			}
			int startI = -1;
			int endI = -1;
			for (int i = 0; i < N; ++i) {
				if (MATRIX[i][j] == 1) {
					startI = i;
					for (int i2 = i + 1; i2 < N; ++i2) {
						if (MATRIX[i2][j] != 1) {
							endI = i2 - 1;
							break;
						}
					}
					endI = endI == -1 ? N - 1 : endI;
					break;
				}
			}
			if (startI == -1) {
				continue;
			}
			int value = list.get(0);
			int remain = value - (endI - startI + 1);
			int upIndex = startI - remain;
			for (int i = 0; i < upIndex; ++i) {
				MATRIX[i][j] = -1;
			}
			int downIndex = endI + remain;
			for (int i = downIndex + 1; i < N; ++i) {
				MATRIX[i][j] = -1;
			}
		}
	}

	private static void setFirstLeft() {
		freedomsLeft = new int[N];
		for (int i = 0; i < N; ++i) {
			List<Integer> list = LEFT.get(i);
			int sum = list.size() - 1;
			for (Integer value : list) {
				sum += value;
			}
			int f = M - sum;
			freedomsLeft[i] = f;
			int j = 0;
			for (Integer value : list) {
				if (value > f) {
					int startJ = j + f;
					int count = value - f;
					for (int k = startJ; k < startJ + count; ++k) {
						MATRIX[i][k] = 1;
					}
				}
				j += value + 1;
			}
		}
	}

	private static void setFirstUp() {
		freedomsUp = new int[M];
		for (int j = 0; j < M; ++j) {
			List<Integer> list = UP.get(j);
			int sum = list.size() - 1;
			for (Integer value : list) {
				sum += value;
			}
			int f = N - sum;
			freedomsUp[j] = f;
			int i = 0;
			for (Integer value : list) {
				if (value > f) {
					int startI = i + f;
					int count = value - f;
					for (int k = startI; k < startI + count; ++k) {
						MATRIX[k][j] = 1;
					}
				}
				i += value + 1;
			}
		}
	}

	private static void drawMatrix(String fileName) {
		BufferedImage image = new BufferedImage(M * d, N * d, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, M * d, N * d);
		g.setColor(Color.BLACK);
		for (int x = d; x < M * d; x += d) {
			g.drawLine(x, 0, x, N * d);
		}
		for (int y = d; y < N * d; y += d) {
			g.drawLine(0, y, M * d, y);
		}
		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < M; ++j) {
				if (MATRIX[i][j] < 0) {
					g.drawLine(j * d, i * d, (j + 1) * d, (i + 1) * d);
					g.drawLine(j * d, (i + 1) * d, (j + 1) * d, i * d);
				} else if (MATRIX[i][j] > 0) {
					g.fillRect(j * d, i * d, d, d);
				}
			}
		}
		try {
			ImageIO.write(image, "PNG", new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean isRowValid(int i) {
		List<Integer> list = LEFT.get(i);
		int valueIndex = 0;
		int counter = 0;
		boolean isBlack = false;
		for (int j = 0; j < M; ++j) {
			if (MATRIX[i][j] > 0) {
				if (isBlack) {
					++counter;
				} else {
					counter = 1;
					isBlack = true;
				}
			} else {
				if (isBlack) {
					int value = valueIndex < list.size() ? list.get(valueIndex) : -1;
					if (value != counter) {
						return false;
					}
					++valueIndex;
					counter = 0;
					isBlack = false;
				}
			}
		}
		if (counter > 0) {
			int value = valueIndex < list.size() ? list.get(valueIndex) : -1;
			if (value != counter) {
				return false;
			}
			++valueIndex;
		}
		return valueIndex == list.size();
	}

	private static boolean isColumnValid(int j) {
		List<Integer> list = UP.get(j);
		int valueIndex = 0;
		int counter = 0;
		boolean isBlack = false;
		for (int i = 0; i < N; ++i) {
			if (MATRIX[i][j] > 0) {
				if (isBlack) {
					++counter;
				} else {
					counter = 1;
					isBlack = true;
				}
			} else {
				if (isBlack) {
					int value = valueIndex < list.size() ? list.get(valueIndex) : -1;
					if (value != counter) {
						return false;
					}
					++valueIndex;
					counter = 0;
					isBlack = false;
				}
			}
		}
		if (counter > 0) {
			int value = valueIndex < list.size() ? list.get(valueIndex) : -1;
			if (value != counter) {
				return false;
			}
			++valueIndex;
		}
		return valueIndex == list.size();
	}

	private static void cleanRow(int i) {
		for (int j = 0; j < M; ++j) {
			MATRIX[i][j] = MATRIX[i][j] == 2 ? 0 : MATRIX[i][j];
		}
	}

	private static void cleanColumn(int j) {
		for (int i = 0; i < N; ++i) {
			MATRIX[i][j] = MATRIX[i][j] == 2 ? 0 : MATRIX[i][j];
		}
	}

	private static boolean checkColumn(int j, int valueIndex, int startI, int f) {
		List<Integer> list = UP.get(j);
		int value = list.get(valueIndex);
		for (int i = startI; i <= startI + f; ++i) {
			int nextIndex = i + value;
			if (MATRIX[i][j] < 0 || i > 0 && MATRIX[i - 1][j] > 0 || nextIndex < N && MATRIX[nextIndex][j] > 0) {
				continue;
			}
			boolean isTrue = true;
			for (int k = i; k < nextIndex; ++k) {
				if (MATRIX[k][j] < 0) {
					isTrue = false;
					break;
				}
			}
			if (!isTrue) {
				continue;
			}
			for (int k = i; k < nextIndex; ++k) {
				MATRIX[k][j] = MATRIX[k][j] == 0 ? 2 : MATRIX[k][j];
			}
			if (valueIndex < list.size() - 1 && checkColumn(j, valueIndex + 1, nextIndex + 1, f - (i - startI))) {
				return true;
			}
			if (isColumnValid(j)) {
				cleanColumn(j);
				return true;
			}
			for (int k = i; k < nextIndex; ++k) {
				MATRIX[k][j] = MATRIX[k][j] == 2 ? 0 : MATRIX[k][j];
			}
		}
		return false;
	}

	private static boolean checkRow(int i, int valueIndex, int startJ, int f) {
		List<Integer> list = LEFT.get(i);
		int value = list.get(valueIndex);
		for (int j = startJ; j <= startJ + f; ++j) {
			int nextIndex = j + value;
			if (MATRIX[i][j] < 0 || j > 0 && MATRIX[i][j - 1] > 0 || nextIndex < M && MATRIX[i][nextIndex] > 0) {
				continue;
			}
			boolean isTrue = true;
			for (int k = j; k < nextIndex; ++k) {
				if (MATRIX[i][k] < 0) {
					isTrue = false;
					break;
				}
			}
			if (!isTrue) {
				continue;
			}
			for (int k = j; k < nextIndex; ++k) {
				MATRIX[i][k] = MATRIX[i][k] == 0 ? 2 : MATRIX[i][k];
			}
			if (valueIndex < list.size() - 1 && checkRow(i, valueIndex + 1, nextIndex + 1, f - (j - startJ))) {
				return true;
			}
			if (isRowValid(i)) {
				cleanRow(i);
				return true;
			}
			for (int k = j; k < nextIndex; ++k) {
				MATRIX[i][k] = MATRIX[i][k] == 2 ? 0 : MATRIX[i][k];
			}
		}
		return false;
	}

	public static void recursive(int i, int j) {
		if (MATRIX[i][j] == 0) {
			MATRIX[i][j] = -1;
			if (checkRow(i, 0, 0, freedomsLeft[i]) && checkColumn(j, 0, 0, freedomsUp[j])) {
				next(i, j);
			}
			MATRIX[i][j] = 1;
			if (checkRow(i, 0, 0, freedomsLeft[i]) && checkColumn(j, 0, 0, freedomsUp[j])) {
				next(i, j);
			}
			MATRIX[i][j] = 0;
		} else {
			next(i, j);
		}
	}

	private static void next(int i, int j) {
		if (j < M - 1) {
			recursive(i, j + 1);
		} else if (i < N - 1) {
			recursive(i + 1, 0);
		} else {
//			drawMatrix("output_images/image_final.png");
			drawMatrix("output_images/chinese_dragon/chinese_dragon_image_final.png");
			System.out.println("Recursive Time=" + (System.currentTimeMillis() - startTimeRecursive) + " ms");
			System.out.println("ENNNDDDD!!!!! Total Time=" + (System.currentTimeMillis() - startTimeTotal) + " ms");
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		startTimeTotal = System.currentTimeMillis();
//		LEFT = loadMatrix("matrix/left.txt");
		LEFT = loadMatrix("matrix/chinese_dragon/chinese_dragon_left.txt");
		N = LEFT.size();
//		UP = loadMatrix("matrix/up.txt");
		UP = loadMatrix("matrix/chinese_dragon/chinese_dragon_up.txt");
		M = UP.size();
		MATRIX = new int[N][M];

		long startTimeDet = System.currentTimeMillis();
		setFirstLeft();
		setFirstUp();
		do {
//			setOneGroupLeft();
//			setOneGroupUp();
//			setLeftBorders();
//			setRightBorders();
//			setUpBorders();
//			setDownBorders();
			setMinMaxRows();
			setMinMaxColumns();
		} while (isSet());
		System.out.println("Determinate time=" + (System.currentTimeMillis() - startTimeDet) + " ms");

//		drawMatrix("output_images/image_det.png");
		drawMatrix("output_images/chinese_dragon/chinese_dragon_image_det.png");
		startTimeRecursive = System.currentTimeMillis();
		recursive(0, 0);
		drawMatrix("image_FFFF.png");
		System.out.println("ENDDDD!!!!!");
	}

	private static boolean isSet() {
		int curBlackCount = 0;
		int curWhiteCount = 0;
		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < M; ++j) {
				curBlackCount += MATRIX[i][j] == 1 ? 1 : 0;
				curWhiteCount += MATRIX[i][j] == -1 ? 1 : 0;
			}
		}
		boolean result = curBlackCount != blackCount || curWhiteCount != whiteCount;
		blackCount = curBlackCount;
		whiteCount = curWhiteCount;
		return result;
	}

	private static void setMinMaxColumns() {
		for (int j = 0; j < M; ++j) {
			List<Integer> list = UP.get(j);
			minIndexes = new ArrayList<Integer>();
			maxIndexes = new ArrayList<Integer>();
			getMinIndexesColumns(j, 0, 0, freedomsUp[j]);
			getMaxIndexesColumns(j, list.size() - 1, N - 1, freedomsUp[j]);
			boolean[] setMatrix = new boolean[N];
			for (int listIndex = 0; listIndex < list.size(); ++listIndex) {
				int value = list.get(listIndex);
				int maxIndex = minIndexes.get(listIndex) + value - 1;
				int minIndex = maxIndexes.get(listIndex);
				for (int i = minIndex; i <= maxIndex; ++i) {
					MATRIX[i][j] = 1;
				}
				int count = maxIndex - minIndex + 1;
				if (count == value) {
					if (minIndex > 0) {
						MATRIX[minIndex - 1][j] = -1;
					}
					if (maxIndex < N - 1) {
						MATRIX[maxIndex + 1][j] = -1;
					}
				}
				for (int i = minIndexes.get(listIndex); i <= maxIndexes.get(listIndex) + value - 1; ++i) {
					setMatrix[i] = true;
				}
			}
			for (int i = 0; i < N; ++i) {
				if (!setMatrix[i]) {
					MATRIX[i][j] = -1;
				}
			}
		}
	}

	private static void setMinMaxRows() {
		for (int i = 0; i < N; ++i) {
			List<Integer> list = LEFT.get(i);
			minIndexes = new ArrayList<Integer>();
			maxIndexes = new ArrayList<Integer>();
			getMinIndexesRows(i, 0, 0, freedomsLeft[i]);
			getMaxIndexesRows(i, list.size() - 1, M - 1, freedomsLeft[i]);
			boolean[] setMatrix = new boolean[M];
			for (int listIndex = 0; listIndex < list.size(); ++listIndex) {
				int value = list.get(listIndex);
				int maxIndex = minIndexes.get(listIndex) + value - 1;
				int minIndex = maxIndexes.get(listIndex);
				for (int j = minIndex; j <= maxIndex; ++j) {
					MATRIX[i][j] = 1;
				}
				int count = maxIndex - minIndex + 1;
				if (count == value) {
					if (minIndex > 0) {
						MATRIX[i][minIndex - 1] = -1;
					}
					if (maxIndex < M - 1) {
						MATRIX[i][maxIndex + 1] = -1;
					}
				}
				for (int j = minIndexes.get(listIndex); j <= maxIndexes.get(listIndex) + value - 1; ++j) {
					setMatrix[j] = true;
				}
			}
			for (int j = 0; j < M; ++j) {
				if (!setMatrix[j]) {
					MATRIX[i][j] = -1;
				}
			}
		}
	}

	private static boolean getMaxIndexesRows(int i, int valueIndex, int startJ, int f) {
		List<Integer> list = LEFT.get(i);
		int value = list.get(valueIndex);
		for (int j = startJ; j >= startJ - f; --j) {
			int nextIndex = j - value;
			if (MATRIX[i][j] < 0 || j < M - 1 && MATRIX[i][j + 1] > 0 || nextIndex >= 0 && MATRIX[i][nextIndex] > 0) {
				continue;
			}
			boolean isTrue = true;
			for (int k = j; k > nextIndex; --k) {
				if (MATRIX[i][k] < 0) {
					isTrue = false;
					break;
				}
			}
			if (!isTrue) {
				continue;
			}
			for (int k = j; k > nextIndex; --k) {
				MATRIX[i][k] = MATRIX[i][k] == 0 ? 2 : MATRIX[i][k];
			}
			if (valueIndex > 0 && getMaxIndexesRows(i, valueIndex - 1, nextIndex - 1, f - (startJ - j))) {
				return true;
			}
			if (isRowValid(i)) {
				for (int k = 0; k < M; ++k) {
					if (MATRIX[i][k] > 0 && (k == 0 || MATRIX[i][k - 1] <= 0)) {
						maxIndexes.add(k);
					}
				}
				cleanRow(i);
				return true;
			}
			for (int k = j; k > nextIndex; --k) {
				MATRIX[i][k] = MATRIX[i][k] == 2 ? 0 : MATRIX[i][k];
			}
		}
		return false;
	}

	private static boolean getMinIndexesRows(int i, int valueIndex, int startJ, int f) {
		List<Integer> list = LEFT.get(i);
		int value = list.get(valueIndex);
		for (int j = startJ; j <= startJ + f; ++j) {
			int nextIndex = j + value;
			if (MATRIX[i][j] < 0 || j > 0 && MATRIX[i][j - 1] > 0 || nextIndex < M && MATRIX[i][nextIndex] > 0) {
				continue;
			}
			boolean isTrue = true;
			for (int k = j; k < nextIndex; ++k) {
				if (MATRIX[i][k] < 0) {
					isTrue = false;
					break;
				}
			}
			if (!isTrue) {
				continue;
			}
			for (int k = j; k < nextIndex; ++k) {
				MATRIX[i][k] = MATRIX[i][k] == 0 ? 2 : MATRIX[i][k];
			}
			if (valueIndex < list.size() - 1 && getMinIndexesRows(i, valueIndex + 1, nextIndex + 1, f - (j - startJ))) {
				return true;
			}
			if (isRowValid(i)) {
				for (int k = 0; k < M; ++k) {
					if (MATRIX[i][k] > 0 && (k == 0 || MATRIX[i][k - 1] <= 0)) {
						minIndexes.add(k);
					}
				}
				cleanRow(i);
				return true;
			}
			for (int k = j; k < nextIndex; ++k) {
				MATRIX[i][k] = MATRIX[i][k] == 2 ? 0 : MATRIX[i][k];
			}
		}
		return false;
	}

	private static boolean getMinIndexesColumns(int j, int valueIndex, int startI, int f) {
		List<Integer> list = UP.get(j);
		int value = list.get(valueIndex);
		for (int i = startI; i <= startI + f; ++i) {
			int nextIndex = i + value;
			if (MATRIX[i][j] < 0 || i > 0 && MATRIX[i - 1][j] > 0 || nextIndex < N && MATRIX[nextIndex][j] > 0) {
				continue;
			}
			boolean isTrue = true;
			for (int k = i; k < nextIndex; ++k) {
				if (MATRIX[k][j] < 0) {
					isTrue = false;
					break;
				}
			}
			if (!isTrue) {
				continue;
			}
			for (int k = i; k < nextIndex; ++k) {
				MATRIX[k][j] = MATRIX[k][j] == 0 ? 2 : MATRIX[k][j];
			}
			if (valueIndex < list.size() - 1
					&& getMinIndexesColumns(j, valueIndex + 1, nextIndex + 1, f - (i - startI))) {
				return true;
			}
			if (isColumnValid(j)) {
				for (int k = 0; k < N; ++k) {
					if (MATRIX[k][j] > 0 && (k == 0 || MATRIX[k - 1][j] <= 0)) {
						minIndexes.add(k);
					}
				}
				cleanColumn(j);
				return true;
			}
			for (int k = i; k < nextIndex; ++k) {
				MATRIX[k][j] = MATRIX[k][j] == 2 ? 0 : MATRIX[k][j];
			}
		}
		return false;
	}

	private static boolean getMaxIndexesColumns(int j, int valueIndex, int startI, int f) {
		List<Integer> list = UP.get(j);
		int value = list.get(valueIndex);
		for (int i = startI; i >= startI - f; --i) {
			int nextIndex = i - value;
			if (MATRIX[i][j] < 0 || i < N - 1 && MATRIX[i + 1][j] > 0 || nextIndex >= 0 && MATRIX[nextIndex][j] > 0) {
				continue;
			}
			boolean isTrue = true;
			for (int k = i; k > nextIndex; --k) {
				if (MATRIX[k][j] < 0) {
					isTrue = false;
					break;
				}
			}
			if (!isTrue) {
				continue;
			}
			for (int k = i; k > nextIndex; --k) {
				MATRIX[k][j] = MATRIX[k][j] == 0 ? 2 : MATRIX[k][j];
			}
			if (valueIndex > 0 && getMaxIndexesColumns(j, valueIndex - 1, nextIndex - 1, f - (startI - i))) {
				return true;
			}
			if (isColumnValid(j)) {
				for (int k = 0; k < N; ++k) {
					if (MATRIX[k][j] > 0 && (k == 0 || MATRIX[k - 1][j] <= 0)) {
						maxIndexes.add(k);
					}
				}
				cleanColumn(j);
				return true;
			}
			for (int k = i; k > nextIndex; --k) {
				MATRIX[k][j] = MATRIX[k][j] == 2 ? 0 : MATRIX[k][j];
			}
		}
		return false;
	}
}
