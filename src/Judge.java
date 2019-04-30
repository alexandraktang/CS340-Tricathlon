import java.util.Random;
import java.util.concurrent.Semaphore;

public class Judge extends Thread {

	public static long time = System.currentTimeMillis();
	
	public static String[] elapsedTimes = new String[main.getNumRacers()]; //total race times
	public static String[] forestTimes = new String[main.getNumRacers()];
	public static String[] mountainTimes = new String[main.getNumRacers()];
	public static String[] riverTimes = new String[main.getNumRacers()];
	
	public static int elapsedCounter = 0; // counter for elapsedTimes
	public static int forestCounter = 0; // counter for forestTimes
	public static int mountainCounter = 0; // counter for mountainTimes
	public static int riverCounter = 0; // counter for riverTimes
	
	
	public Judge() {
		
	}
	
	public Judge(String judgeName) {
		super(judgeName);
		start();
	}
	
	public void msg(String m) {
		System.out.println("[" + (System.currentTimeMillis()-time) + "] "
				+ getName() +": " + m);
	}
	
	//Generate random number between two given numbers
	public static int randomNumber(int min, int max) {
		Random rand = new Random();
		return rand.nextInt(max - min + 1) + min;
	}
	
	
	
	//Semaphores for River
	Semaphore ready = Racer.ready; //waits for a group to signal it's ready
	Semaphore available = Racer.available; //to signal a group the lines are available for them to swim
	Semaphore done = Racer.done; // waits for Racers to let the Judge know they're done
	
	//Variables for River
	boolean lastGroup = Racer.lastGroup; // if last group
	
	//Semaphores for Go Home
	static Semaphore report = new Semaphore(0); // releases each Racer from waiting for the two reports
	Semaphore lastGoHome = Racer.lastGoHome; // signals the last Racer to go home
	Semaphore allGone = Racer.allGone; // waits for a signal that all Racers have gone home
	
	
	@Override
	public void run() {
		
		
		//RIVER
		while(true) {
			
			try {
				msg("Waiting for a group to be ready");
				ready.acquire(); //waits for a group to be ready
			} catch (InterruptedException e) {
				System.out.println("Group not ready.");
			}
			
			for (int i = 0; i < main.getNumLines(); i++) {
				available.release(); //signal all waiting Racers river is available to start crossing
			}
			
			for(int j = 0; j < main.getNumLines(); j++) {
				try {
					done.acquire(); // waits for each Racer to finish crossing
				} catch (InterruptedException e) {
					System.out.println("Not all racers are finished crossing");
				}
			}
			
			// if last group
			if (lastGroup = true && available.getQueueLength() == Racer.lastGroupLeft
					&& Racer.curRacers - Racer.numDone == Racer.lastGroupLeft) {
				msg("This is the last group to cross the river.");
				break; // leaves the while loop as there will be no groups after
			}	
		}
		
		//for the last group if not evenly divisible
		if (main.getNumRacers() % main.getNumLines() != 0 ) {
			for (int i = 0; i < main.getNumLines(); i++) {
				available.release(); //signal all waiting Racers river is available to start crossing
			}
			
			// for as many racers as there are left in the last group
			for (int j = 0; j < Racer.lastGroupLeft; j++) {
				try {
					done.acquire(); // waits for each Racer to finish crossing
				} catch (InterruptedException e) {
					System.out.println("Not all racers are finished crossing");
				} 
			}
		}
		
		//GO HOME
		for (int i = 0; i < main.getNumRacers(); i++) {
			msg("Releasing racer " + i);
			report.release(); // releases each racer from the report queue
		}
		printReport1(); // prints the first report of total elapsed times
		System.out.println("");
		System.out.println("");
		printReport2(); // prints the second report of individual obstacle times
		lastGoHome.release(); // tells last runner to go home
		try {
			msg("Waiting for all racers to leave.");
			allGone.acquire(); // waits for all runners to go home
			msg("All the racers have left.");
		} catch (InterruptedException e) {
			System.out.println("Problem with allGone sem.");
		} 
	}
	
	//updates an array with each Racer's time when they finish the Forest
	public static void getForestTime(String name, long forestTime) {
		forestTimes[forestCounter] = name + ": " + forestTime;	
		forestCounter++;
	}
	
	//updates an array with each Racer's time when they finish the Mountain
	public static void getMountainTime(String name, long mountainTime) {
		mountainTimes[mountainCounter] = name + ": " + mountainTime;
		mountainCounter++;
	}
	
	//updates an array with each Racer's time when they finish the River
	public static void getRiverTime(String name, long riverTime) {
		riverTimes[riverCounter] = name + ": " + riverTime;
		riverCounter++;
	}
	
	//updates an array with each Racer's time when they finish the entire race
	public static void getTimeElapsed(String name, long elapsedTime) {
		elapsedTimes[elapsedCounter] = name + ": " + elapsedTime;
		elapsedCounter++;
	}
	
	//traverses through the array to print each Racer's total elapsed time in the race
	public static void printReport1() {
		System.out.println("REPORT 1");
		System.out.println("These are the total times: ");
		for (int i = 0; i < main.getNumRacers(); i++) {
			System.out.println(elapsedTimes[i]);
		}
	}
	
	//traverses through each obstacle's arrays to print the times for each Racer in each obstacle
	public static void printReport2() {
		System.out.println("REPORT 2");
		System.out.println("These are the forest times: ");
		for(int i = 0; i < main.getNumRacers(); i++) {
			System.out.println(forestTimes[i]);
		}
		
		System.out.println("These are the mountain times: ");
		for(int i = 0; i < main.getNumRacers(); i++) {
			System.out.println(mountainTimes[i]);
		}
		
		System.out.println("These are the river times: ");
		for(int i = 0; i < main.getNumRacers(); i++) {
			System.out.println(riverTimes[i]);
		}
	}
	
}
