package co.edu.unal.paralela;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Una clase 'envoltorio' (wrapper) para varios métodos analíticos.
 */
public final class StudentAnalytics {
    /**
     * Calcula secuencialmente la edad promedio de todos los estudientes registrados y activos 
     * utilizando ciclos.
     *
     * @param studentArray Datos del estudiante para la clase.
     * @return Edad promedio de los estudiantes registrados
     */
    
 	private final static String[] firstNames = {"Hitagi","Aldebaran","Emilia","Hikari","Lelouch","Felipe"};   
	private final static String[] lastNames = {"Senjougahara","Reinols","Tan","Songoki","Britania","Flores"};

	
	private static void printResults(String name, long timeInNanos, double sum) {
		System.out.printf("%s completed in %8.3f milliseconds, with sum = %8.5f \n", name, timeInNanos / 1e6, sum);
	}

	public static double averageAgeOfEnrolledStudentsImperative(final Student[] studentArray) {
		long startTime = System.nanoTime();
		List<Student> activeStudents = new ArrayList<Student>();

		for (Student s : studentArray) 
			if (s.checkIsCurrent()) activeStudents.add(s);

		double ageSum = 0.0;
		for (Student s : activeStudents) ageSum += s.getAge();
		double retVal = ageSum / (double) activeStudents.size();
		
		long timeInNanos = System.nanoTime() - startTime;
		printResults("seqIteration", timeInNanos, retVal);
		return retVal;
	}


    /**
     * PARA HACER calcular la edad promedio de todos los estudiantes registrados y activos usando
     * streams paralelos. Debe reflejar la funcionalidad de 
     * averageAgeOfEnrolledStudentsImperative. Este método NO debe utilizar ciclos.
     *
     * @param studentArray Datos del estudiante para esta clase.
     * @return Edad promedio de los estudiantes registrados
     */
	public static double averageAgeOfEnrolledStudentsParallelStream(final Student[] studentArray) {
		long startTime = System.nanoTime();
		double retVal = Stream.of(studentArray)
				.parallel()
				.filter(s -> s.checkIsCurrent())
				.mapToDouble(a -> a.getAge())
				.average()
				.getAsDouble();
		
		long timeInNanos = System.nanoTime() - startTime;
		printResults("parStreams", timeInNanos, retVal);
		return retVal;
}
    /**
     * Calcula secuencialmente -usando ciclos- el nombre más común de todos los estudiantes 
     * que no están activos en la clase.
     *
     * @param studentArray Datos del estudiante para esta clase.
     * @return Nombre más común de los estudiantes inactivos.
     */
    public String mostCommonFirstNameOfInactiveStudentsImperative(
            final Student[] studentArray) {
        List<Student> inactiveStudents = new ArrayList<Student>();

        for (Student s : studentArray) {
            if (!s.checkIsCurrent()) {
                inactiveStudents.add(s);
            }
        }

        Map<String, Integer> nameCounts = new HashMap<String, Integer>();

        for (Student s : inactiveStudents) {
            if (nameCounts.containsKey(s.getFirstName())) {
                nameCounts.put(s.getFirstName(),
                        new Integer(nameCounts.get(s.getFirstName()) + 1));
            } else {
                nameCounts.put(s.getFirstName(), 1);
            }
        }

        String mostCommon = null;
        int mostCommonCount = -1;
        for (Map.Entry<String, Integer> entry : nameCounts.entrySet()) {
            if (mostCommon == null || entry.getValue() > mostCommonCount) {
                mostCommon = entry.getKey();
                mostCommonCount = entry.getValue();
            }
        }

        return mostCommon;
    }

    /**
     * PARA HACER calcula el nombre más común de todos los estudiantes que no están activos
     * en la clase utilizando streams paralelos. Debe reflejar la funcionalidad 
     * de mostCommonFirstNameOfInactiveStudentsImperative. Este método NO debe usar ciclos
     *
     * @param studentArray Datos de estudiantes para la clase.
     * @return Nombre más comun de los estudiantes inactivos.
     */
	public String mostCommonFirstNameOfInactiveStudentsParallelStream(final Student[] studentArray) {
		return Stream.of(studentArray)
                .parallel()
                .filter(s -> !s.checkIsCurrent())
                .map(s -> s.getFirstName())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .parallel()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .get()
                .getKey();
}

    /**
     * calcula secuencialmente el número de estudiantes que han perdido el curso 
     * que son mayores de 20 años. Una calificación de perdido es cualquiera por debajo de 65 
     * 65. Un estudiante ha perdido el curso si tiene una calificación de perdido 
     * y no está activo en la actuialidad
     *
     * @param studentArray Datos del estudiante para la clase.
     * @return Cantidad de calificacione sperdidas de estudiantes mayores de 20 años de edad.
     */
    public int countNumberOfFailedStudentsOlderThan20Imperative(
            final Student[] studentArray) {
        int count = 0;
        for (Student s : studentArray) {
            if (!s.checkIsCurrent() && s.getAge() > 20 && s.getGrade() < 65) {
                count++;
            }
        }
        return count;
    }

    /**
     * PARA HACER calcular el número de estudiantes que han perdido el curso 
     * que son mayores de 20 años de edad . una calificación de perdido está por debajo de 65. 
     * Un estudiante ha perdido el curso si tiene una calificación de perdido 
     * y no está activo en la actuialidad. Debe reflejar la funcionalidad de 
     * countNumberOfFailedStudentsOlderThan20Imperative. El método no debe usar ciclos.
	 *
     * @param studentArray Datos del estudiante para la clase.
     * @return Cantidad de calificacione sperdidas de estudiantes mayores de 20 años de edad.
     */
	public int countNumberOfFailedStudentsOlderThan20ParallelStream(final Student[] studentArray) {
		return (int) Stream.of(studentArray)
	            .parallel()
	            .filter(s -> (s.getAge() > 20) && (!s.checkIsCurrent() && (s.getGrade() < 65)))
	            .count();
}
        
        public static void main(final String[] argv) {
		
		final int N_STUDENTS = 2000000;
        final int N_CURRENT_STUDENTS = 600000;
		
		Student[] students = new Student[N_STUDENTS];
		Random r = new Random(123);
		
		for (int s = 0; s < N_STUDENTS; s++) {
		    final String firstName = firstNames[r.nextInt(firstNames.length)];
		    final String lastName = lastNames[r.nextInt(lastNames.length)];
		    final double age = r.nextDouble() * 100.0;
		    final int grade = 1 + r.nextInt(100);
		    final boolean current = (s < N_CURRENT_STUDENTS);
		
		    students[s] = new Student(firstName, lastName, age, grade, current);
		}
		
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");
		for (int numRun = 0; numRun < 5; numRun++) {
			System.out.printf("Run %d\n", numRun);
			averageAgeOfEnrolledStudentsImperative(students);
			averageAgeOfEnrolledStudentsParallelStream(students);
		}
		
}
}
