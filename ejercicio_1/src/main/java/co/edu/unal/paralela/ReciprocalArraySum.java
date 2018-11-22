package co.edu.unal.paralela;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
//con ayuda de http://www.congqiyuan.com/JavaStack
/**
 * Clase que contiene los métodos para implementar la suma de los recíprocos de un arreglo usando paralelismo.
 */
public final class ReciprocalArraySum {
        public static final String ERROR_MSG ="array debe ser mayor a cero";
        public static final int DEFAULT_N = 100_000_000;
    /**
     * Constructor.
     */
    private ReciprocalArraySum() {
    }

    /**
     * Calcula secuencialmente la suma de valores recíprocos para un arreglo.
     *
     * @param input Arreglo de entrada
     * @return La suma de los recíprocos del arreglo de entrada
     */
		 protected static double seqArraySum(final double[] input) {
	 		long startTime = System.nanoTime();
	 		double sum = 0;
	 		for (int i = 0; i < input.length; i++)
	 			sum += 1 / input[i];
	 		long timeInNanos = System.nanoTime() - startTime;
	 		printResults("seqArraySum", timeInNanos, sum);
	 		return sum;
	 }
	private static void printResults(String name, long timeInNanos, double sum) {
		System.out.printf("%s completed in %8.3f milliseconds, with sum = %8.5f \n", name, timeInNanos / 1e6, sum);
}
    /**
     * calcula el tamaño de cada trozo o sección, de acuerdo con el número de secciones para crear
     * a través de un número dado de elementos.
     *
     * @param nChunks El número de secciones (chunks) para crear
     * @param nElements El número de elementos para dividir
     * @return El tamaño por defecto de la sección (chunk)
     */
    private static int getChunkSize(final int nChunks, final int nElements) {
        // Función techo entera
        return (nElements + nChunks - 1) / nChunks;
    }

    /**
     * Calcula el índice del elemento inclusivo donde la sección/trozo (chunk) inicia,
     * dado que hay cierto número de secciones/trozos (chunks).
     *
     * @param chunk la sección/trozo (chunk) para cacular la posición de inicio
     * @param nChunks Cantidad de seciiones/trozos (chunks) creados
     * @param nElements La cantidad de elementos de la sección/trozo que debe atravesarse
     * @return El indice inclusivo donde esta sección/trozo (chunk) inicia en el conjunto de
     *         nElements
     */
    private static int getChunkStartInclusive(final int chunk,
            final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    /**
     * Calcula el índice del elemento exclusivo que es proporcionado al final de la sección/trozo (chunk),
     * dado que hay cierto número de secciones/trozos (chunks).
     *
     * @param chunk LA sección para calcular donde termina
     * @param nChunks Cantidad de seciiones/trozos (chunks) creados
     * @param nElements La cantidad de elementos de la sección/trozo que debe atravesarse
     * @return El índice de terminación exclusivo para esta sección/trozo (chunk)
     */
    private static int getChunkEndExclusive(final int chunk, final int nChunks,
            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    /**
     * Este pedazo de clase puede ser completada para para implementar el cuerpo de cada tarea creada
     * para realizar la suma de los recíprocos del arreglo en paralelo.
     */
    private static class ReciprocalArraySumTask extends RecursiveAction {
		private static final long serialVersionUID = -2993838388487150643L;
		static int SEQUENTIAL_THRESHOLD = 1000;
		//static int SEQUENTIAL_THRESHOLD = 100000;
		private final int startIndexInclusive;
		private final int endIndexExclusive;
		private final double[] input;
		private double value = 0;

		ReciprocalArraySumTask(final int setStartIndexInclusive, final int setEndIndexExclusive, final double[] setInput) {
			this.startIndexInclusive = setStartIndexInclusive;
			this.endIndexExclusive = setEndIndexExclusive;
			this.input = setInput;
		}

		public double getValue() {
			return value;
		}

		@Override
		protected void compute() {
			if (endIndexExclusive - startIndexInclusive <= SEQUENTIAL_THRESHOLD) {
				for (int i = startIndexInclusive; i<endIndexExclusive; i++) 
					value += 1 / input[i];
			} else {
				ReciprocalArraySumTask left = new ReciprocalArraySumTask(startIndexInclusive, (startIndexInclusive+endIndexExclusive)/2, input);
				ReciprocalArraySumTask right = new ReciprocalArraySumTask((startIndexInclusive+endIndexExclusive)/2, endIndexExclusive, input);
				left.fork();      //async
                right.compute();
                left.join();
				value = left.value + right.value;
			}
			
		}
}

    /**
     * Para hacer: Modificar este método para calcular la misma suma de recíprocos como le realizada en
     * seqArraySum, pero utilizando dos tareas ejecutándose en paralelo dentro del framework ForkJoin de Java
     * Se puede asumir que el largo del arreglo de entrada
     * es igualmente divisible por 2.
     *
     * @param input Arreglo de entrada
     * @return La suma de los recíprocos del arreglo de entrada
     */
	protected static double parArraySum(final double[] input) {
		assert input.length % 2 == 0;

		long startTime = System.nanoTime();
		ReciprocalArraySumTask t = new ReciprocalArraySumTask (0, input.length, input);
		ForkJoinPool.commonPool().invoke(t);
		double sum = t.getValue();
		long timeInNanos = System.nanoTime() - startTime;
		printResults("parArraySum", timeInNanos , sum);
		return sum;
}

    /**
     * Para hacer: extender el trabajo hecho para implementar parArraySum que permita utilizar un número establecido
     * de tareas para calcular la suma del arreglo recíproco.
     * getChunkStartInclusive y getChunkEndExclusive pueden ser útiles para cacular
     * el rango de elementos indice que pertenecen a cada sección/trozo (chunk).
     *
     * @param input Arreglo de entrada
     * @param numTasks El número de tareas para crear
     * @return La suma de los recíprocos del arreglo de entrada
     */

	protected static double parManyTaskArraySum(final double[] input, final int numTasks) {
		double sum = 0;
		List <ReciprocalArraySumTask> subtasks = new ArrayList<>();
		 
		for (int i = 0; i < numTasks; i++)  {
			int lo = getChunkStartInclusive(i, numTasks, input.length);
			int hi = getChunkEndExclusive(i, numTasks, input.length);		
			subtasks.add(new ReciprocalArraySumTask(lo, hi, input));
		}
		
		ForkJoinTask.invokeAll(subtasks);	
		
		for(int j = 0; j < subtasks.size(); j++)
            sum += subtasks.get(j).getValue();
		
		return sum;
}
        

	public static void main(final String[] argv) {
		int n;
		if(argv.length != 0) {
			try {
				n = Integer.parseInt(argv[0]);
				if(n <= 0) {
                                    
                                    //public static final String ERROR_MSG ="array debe ser mayor a cero";
                                    //public static final int DEFAULT_N = 100_000_000;
					System.out.println(ERROR_MSG);
					n = DEFAULT_N;
				}
			} catch (NumberFormatException e) {
				System.out.println(ERROR_MSG);
				n = DEFAULT_N;
			}
		} else {
			n = DEFAULT_N;
		}
		
		double[] X = new double[n];
		for(int i=0; i<n; i++) {
			X[i] =(i+1);
		}
		
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");
		for (int numRun = 0; numRun < 5; numRun++) {
			System.out.printf("Run %d\n", numRun);
			seqArraySum(X);
			parArraySum(X);
		}
		
}
}
