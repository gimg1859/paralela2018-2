package co.unal.edu.unal.paralela;

import static edu.rice.pcdp.PCDP.forall2dChunked;
import static edu.rice.pcdp.PCDP.forseq2d;
import java.util.Random;

/**
 * Clase envolvente pata implementar de forma eficiente la multiplicación dde matrices en paralelo.
 */
public final class MatrixMultiply {
    /**
     * Constructor por omisión.
     */
    private MatrixMultiply() {
    }

    /**
     * Realiza una multiplicación de matrices bidimensionales (A x B = C) de forma secuencial.
     *
     * @param A Una matriz de entrada con dimensiones NxN
     * @param B Una matriz de entrada con dimensiones NxN
     * @param C Matriz de salida
     * @param N Tamaño de las matrices de entrada
     */
    public static void seqMatrixMultiply(final double[][] A, final double[][] B,
            final double[][] C, final int N) {
        long startTime = System.nanoTime();
        forseq2d(0, N - 1, 0, N - 1, (i, j) -> {
            C[i][j] = 0.0;
            for (int k = 0; k < N; k++) {
                C[i][j] += A[i][k] * B[k][j];
            }
        });
          long timeInNanos = System.nanoTime() - startTime;
          printResults("seqMatrixMultiply", timeInNanos, C[N-1][N-1]);
    }

    /**
     * Realiza una multiplicación de matrices bidimensionales (A x B = C) de forma paralela.
     *
     * @param A Una matriz de entrada con dimensiones NxN
     * @param B Una matriz de entrada con dimensiones NxN
     * @param C Matriz de salida
     * @param N amaño de las matrices de entrada
     */
    public static void parMatrixMultiply(final double[][] A, final double[][] B, final double[][] C, final int N) {
		long startTime = System.nanoTime();

		forall2dChunked(0, N - 1, 0, N - 1, (i, j) -> {
            C[i][j] = 0.0;
            for (int k = 0; k < N; k++) {
                C[i][j] += A[i][k] * B[k][j];
            }
        });
        
        long timeInNanos = System.nanoTime() - startTime;
        printResults("parMatrixMultiply", timeInNanos, C[N-1][N-1]);
    }
    
	private static void printResults(String name, long timeInNanos, double sum) {
		System.out.printf("%s completed in %8.3f milliseconds, with C[N-1][N-1] = %8.5f \n", name, timeInNanos / 1e6, sum);
	}
	
	private static double[][] createMatrix(final int N) {
		final double[][] input = new double[N][N];
		final Random rand = new Random(350);

		for (int i = 0; i < N; i++) 
			for (int j = 0; j < N; j++) 
				input[i][j] = rand.nextInt(100);

		return input;
	}
	
	public static void main(final String[] argv) {
		int N = 512;
		final double[][] A = createMatrix(N);
		final double[][] B = createMatrix(N);
		final double[][] C = new double[N][N];
		
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");
		for (int numRun = 0; numRun < 5; numRun++) {
			System.out.printf("Run %d\n", numRun);
			seqMatrixMultiply(A, B, C, N);
			parMatrixMultiply(A, B, C, N);
		}
		
	}
	
}
