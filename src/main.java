import java.util.Scanner;

public class main {
	
	public static int numRacers = 0;
	public static int numLines = 0;
	
	public static int getNumRacers() {
		return numRacers;
	}
	
	public static int getNumLines() {
		return numLines;
	}
	
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		
		System.out.println("How many racers?: "); // asks the user for the number of racers
		numRacers = input.nextInt(); // updates numRacers with the user's input

		System.out.println("How many lines?: "); // asks the user for the number of lines
		numLines = input.nextInt(); // updates numLines with the user's input
		
		for (int i = 0; i < numRacers; i++) {
			Racer racer = new Racer("Racer" + i); // creates as many Racers as the user inputs
		}
		
		Judge theJudge = new Judge("TheJudge"); // creates one Judge
	}

}
