package flappyBirdAI;

import java.util.Random;
import java.util.function.Function;
import java.lang.reflect.Method;

public class Matrix {
	public int row;
	public int col;
	public float[][] data;
	public Matrix(int row, int col) {
		this.row = row;
		this.col = col;
		this.data = new float[row][col];
		for (int y = 0; y < row; y++) {
			for (int x = 0; x < col; x++) {
				this.data[y][x] = 0.0f;
			}
		}
	}
	
	public static Matrix FromArray(float[] array) {
		Matrix resultMatrix = new Matrix(array.length, 1);
		
		for (int y = 0; y < array.length; y++) {
			resultMatrix.data[y][0] = array[y];
		}
		
		return resultMatrix;
	}
	
	public float[] ToArray() {
		float[] returnArray = new float[this.row*this.col];
		
		for (int y = 0; y < this.row; y++) {
			for (int x = 0; x < this.col; x++) {
				returnArray[(y*this.col)+x] = this.data[y][x];
			}
		}
		
		return returnArray;
	}
	
	public static float[] ToArray(Matrix m) {
		float[] returnArray = new float[m.row*m.col];
		
		for (int y = 0; y < m.row; y++) {
			for (int x = 0; x < m.col; x++) {
				returnArray[(y*m.col)+x] = m.data[y][x];
			}
		}
		
		return returnArray;
	}
	
	public static void Show(Matrix m) {
		System.out.println("{");
		for (int y = 0; y < m.row; y++) {
			System.out.print("[ " + m.data[y][0]);
			for (int x = 1; x < m.col; x++) {
				System.out.print(", " + m.data[y][x]);
			}
			System.out.println("]");
		}
		System.out.println("}");
	}
	
	public void show() {
		System.out.println("{");
		for (int y = 0; y < this.row; y++) {
			System.out.print("[ " + this.data[y][0]);
			for (int x = 1; x < this.col; x++) {
				System.out.print(", " + this.data[y][x]);
			}
			System.out.println("]");
		}
		System.out.println("}");
	}
	
	public void RandomFill(long seed) {
		Random random = new Random(seed);
		for (int y = 0; y < this.row; y++) {
			for (int x = 0; x < this.col; x++) {
				if (random.nextBoolean()) {
					this.data[y][x] = (-1*random.nextFloat());
				}else {
					this.data[y][x] = (1*random.nextFloat());
				}
				
			}
		}
	}
	// ----------------------Transposing-----------------------------
	public static Matrix Transpose(Matrix m) {
		Matrix resultMatrix = new Matrix(m.col, m.row);
		
		for (int y = 0; y < m.row; y++) {
			for (int x = 0; x < m.col; x++) {
				resultMatrix.data[x][y] = m.data[y][x];
			}
		}
		
		return resultMatrix;
	}
	
	// ----------------------Multiplying-----------------------------
	public static Matrix Multiply(Matrix m1, Matrix m2) {
		Matrix resultMatrix = new Matrix(m1.row, m1.col);
		
		if (m1.row == m2.row && m1.col == m2.col) {
			for (int y = 0; y < m1.row; y++) {
				for (int x = 0; x < m1.col; x++) {
					resultMatrix.data[y][x] = m1.data[y][x] * m2.data[y][x];
				}
			}
		}
		
		return resultMatrix;
	}
	public static Matrix Multiply(Matrix m1, float n) {
		Matrix resultMatrix = new Matrix(m1.row, m1.col);
		
		for (int y = 0; y < m1.row; y++) {
			for (int x = 0; x < m1.col; x++) {
				resultMatrix.data[y][x] = m1.data[y][x] * n;
			}
		}
		
		return resultMatrix;
	}
	public void multiply(Matrix m) {
		if (this.row == m.row && this.col == m.col) {
			for (int y = 0; y < this.row; y++) {
				for (int x = 0; x < this.col; x++) {
					this.data[y][x] *= m.data[y][x];
				}
			}
		}
		
	}
	public void multiply(float n) {
		for (int y = 0; y < this.row; y++) {
			for (int x = 0; x < this.col; x++) {
				this.data[y][x] *= n;
			}
		}
	}
	
	// ----------------------Adding-----------------------------
	public static Matrix Add(Matrix m1, Matrix m2) {
		Matrix resultMatrix = new Matrix(m1.row, m1.col);
		
		if (m1.row == m2.row && m1.col == m2.col) {
			for (int y = 0; y < m1.row; y++) {
				for (int x = 0; x < m1.col; x++) {
					resultMatrix.data[y][x] = m1.data[y][x] + m2.data[y][x];
				}
			}
		}
		
		return resultMatrix;
	}
	public static Matrix Add(Matrix m1, float n) {
		Matrix resultMatrix = new Matrix(m1.row, m1.col);
		
		for (int y = 0; y < m1.row; y++) {
			for (int x = 0; x < m1.col; x++) {
				resultMatrix.data[y][x] = m1.data[y][x] + n;
			}
		}
		
		return resultMatrix;
	}
	public void add(Matrix m) {
		if (this.row == m.row && this.col == m.col) {
			for (int y = 0; y < this.row; y++) {
				for (int x = 0; x < this.col; x++) {
					this.data[y][x] += m.data[y][x];
				}
			}
		}
		
	}
	public void add(float n) {
		for (int y = 0; y < this.row; y++) {
			for (int x = 0; x < this.col; x++) {
				this.data[y][x] += n;
			}
		}
	}
	
	// -------------------Subtracting--------------------------
	public static Matrix Subtract(Matrix m1, Matrix m2) {
		Matrix resultMatrix = new Matrix(m1.row, m1.col);
		
		if (m1.row == m2.row && m1.col == m2.col) {
			for (int y = 0; y < m1.row; y++) {
				for (int x = 0; x < m1.col; x++) {
					resultMatrix.data[y][x] = m1.data[y][x] - m2.data[y][x];
				}
			}
		}
		
		return resultMatrix;
	}
	public static Matrix Subtract(Matrix m1, float n) {
		Matrix resultMatrix = new Matrix(m1.row, m1.col);
		
		for (int y = 0; y < m1.row; y++) {
			for (int x = 0; x < m1.col; x++) {
				resultMatrix.data[y][x] = m1.data[y][x] - n;
			}
		}
		
		return resultMatrix;
	}
	public void subtract(Matrix m) {
		if (this.row == m.row && this.col == m.col) {
			for (int y = 0; y < this.row; y++) {
				for (int x = 0; x < this.col; x++) {
					this.data[y][x] -= m.data[y][x];
				}
			}
		}
		
	}
	public void subtract(float n) {
		for (int y = 0; y < this.row; y++) {
			for (int x = 0; x < this.col; x++) {
				this.data[y][x] -= n;
			}
		}
	}
	
	
	// --------------------Matrix Product---------------------
	public static Matrix MatrixProduct(Matrix m1, Matrix m2) {
		Matrix resultMatrix = new Matrix(m1.row, m2.col);
		
		if (m1.row == m2.col) {
			
		}
		for (int y = 0; y < resultMatrix.row; y++) {
			for (int x = 0; x < resultMatrix.col; x++) {
				for (int i = 0; i < m1.col; i++) {
					resultMatrix.data[y][x] += (m1.data[y][i] * m2.data[i][x]);
				}
			}
		}
		
		return resultMatrix;
	}
	
	public static Matrix Map(Matrix m, Function<Float, Float> func) {
		Matrix resultMatrix = new Matrix(m.row, m.col);
		
		for (int y = 0; y < m.row; y++) {
			for (int x = 0; x < m.col; x++) {
				resultMatrix.data[y][x] = func.apply(m.data[y][x]);
			}
		}
		
		return resultMatrix;
	}
	
	// ----------------------Mapping------------------------
	public void map(Function<Float, Float> func) {
		
		for (int y = 0; y < this.row; y++) {
			for (int x = 0; x < this.col; x++) {
				this.data[y][x] = func.apply(this.data[y][x]);
			}
		}
	}

}

