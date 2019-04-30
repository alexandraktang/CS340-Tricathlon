import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.*;

public class Racer extends Thread {
	public static LinkedList<Racer> finishLine = new LinkedList<Racer>();
	public static long time = System.currentTimeMillis();
	
	//Times for Judge
	private static long startTimeElapsed = 0; // time starting race
	private static long endTimeElapsed = 0; // time ending race
	private static long timeElapsed = 0; //time for entire race with rest times
	
	private static long startForestTime = 0; // time starting forest
	private static long endForestTime = 0; // time ending forest
	private static long forestTime = 0; // total time for forest
	
	private static long startMountainTime = 0; // time starting mountain
	private static long endMountainTime = 0; // time ending mountain
	private static long mountainTime = 0; // total time for mountain
	
	private static long startRiverTime = 0; // time starting river
	private static long endRiverTime = 0; // time ending river
	private static long riverTime = 0; // total time for river
	
	
	static Semaphore timeME = new Semaphore(1); // mutual exclusion over Judge time counters
	
	//Semaphore for Mountain
	static Semaphore mutex = new Semaphore(1); // mutual exclusion binary semaphore
	
	//Semaphores for River (+ mutex above)
	static Semaphore otherME = new Semaphore(1); // mutual exclusion over numDone counter
	static Semaphore group = new Semaphore(0); // waits until enough racers in a group
	static Semaphore ready = new Semaphore(0); // signals when a group is ready
	static Semaphore available = new Semaphore(0); // waits for lines in river to be available
	static Semaphore done = new Semaphore(0); // signals Judge when Racer is done crossing river
	
	//Variables for River
	static int curRacers = 0; // # of current racers in river
	static int numDone = 0; // # of racers finished with process before crossing river
	static int lastGroupLeft = 0; // # of racers in last group, important for when numRacers % numLines != 0
	static boolean lastGroup = false; // in conjunction with lastGroupLeft, tells the Judge 
									 // whether or not to enter last round
	
	//Semaphores for Go Home
	Semaphore report = Judge.report; //wait for report from Judge after river
	static Semaphore lastGoHome = new Semaphore(0); // wait for Judge to tell last Racer to go home
	private static Semaphore goHome = new Semaphore(0); // wait for racer above to go home
	static Semaphore allGone = new Semaphore(0); // signals Judge that all Racers have gone home
	
	
	public Racer(String racerName) {
		super(racerName);
		start();
	}
	
	public void msg(String m) {
		System.out.println("[" + (System.currentTimeMillis()-time) + "] "
				+ getName() + ": " + m);
	}
	
	
	
	
	//Generate random number between two given numbers
	public int randomNumber(int min, int max) {
		Random rand = new Random();
		return rand.nextInt(max - min + 1) + min;
	}
	
	//Generate random string for forest's magic word
	public String magicWordGenerator() {  
	    int a = 97; 
	    int d = 100;
	    int length = 4;
	    Random rand = new Random();
	    StringBuilder wordInProgress = new StringBuilder(length);
	    for (int i = 0; i < length; i++) {
	        int randomLetter = (int) (rand.nextFloat() * (d - a + 1)) + a;
	        wordInProgress.append((char) randomLetter);
	    }
	    String magicWord = wordInProgress.toString();
	    return magicWord;
	}
	
	
	
	
	
	
	
	@Override
	public void run() {
		
		startTimeElapsed = System.currentTimeMillis()-time;
		
		
		
		
		
		//REST BEFORE FOREST
		try {
			msg("Resting before forest");
			sleep(randomNumber(5000,10000));
		} catch (InterruptedException e) {
			
		}
		
		
		
		
		
		
		this.startForestTime = System.currentTimeMillis()-time;
		
		//THE FOREST
		int defaultPriority = getPriority();
		setPriority(getPriority() + randomNumber(0,4));
		
		//Search Forest for Magic Word
		File forest = new File("Forest.txt");
		String magicWord = magicWordGenerator();
		boolean mwFound = false;
		Scanner scanner = null;
		
		//If file not found
		try {
			scanner = new Scanner(forest);
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		}
		
		//Search forest for magic word
		while (scanner.hasNextLine()) {
		   final String currentLine = scanner.nextLine();
		   if (currentLine.contains(magicWord)) { 
		       msg("Magic word found!");
		       mwFound = true;
		       break;
		   }
		}
		
		//If magic word wasn't found
		if (mwFound != true) {
			msg("Couldn't find magic word; yielding!");
			yield();
			yield();
		}
		
		//Reset priority
		setPriority(defaultPriority);
		
		this.endForestTime = System.currentTimeMillis()-time;
		this.forestTime = this.endForestTime - this.startForestTime;
		
		//Mutual exclusion over inputting values into the Judge's array for Forest times
		try {
			timeME.acquire();
		} catch (InterruptedException e2) {
			System.out.println("Problem getting mutex");
		}
		Judge.getForestTime(this.getName(), this.forestTime);
		timeME.release();
		
		
		
		
		//REST BEFORE MOUNTAIN
		try {
			msg("Resting before mountain");
			sleep(randomNumber(2000,5000));
		} catch (InterruptedException e) {
			
		}
		
			
		
		
		this.startMountainTime = System.currentTimeMillis() - time;
		
		//THE MOUNTAIN
		try {
			mutex.acquire();
			sleep(randomNumber(3000,5000));
		} catch (InterruptedException e1) {
			msg("Problem crossing passage!");
		}
		mutex.release();
		msg("Crossed the passage!");
		
		this.endMountainTime = System.currentTimeMillis() - time;
		this.mountainTime = endMountainTime - startMountainTime;
		
		
		
		
		
		//Mutual exclusion over inputting values into the Judge's array for Mountain times
		try {
			timeME.acquire();
		} catch (InterruptedException e2) {
			System.out.println("Problem getting mutex");
		}
		Judge.getMountainTime(this.getName(), this.mountainTime);
		timeME.release();
		
		
		
		
		
		
		
		//REST BEFORE THE RIVER
		try {
			msg("Resting before river");
			sleep(randomNumber(2000,3000));
		} catch (InterruptedException e) {
			msg("Problem resting before river.");
		}
		
		
		
		
		
		
		
		//THE RIVER
		this.startRiverTime = System.currentTimeMillis() - time;
		
		try {
			mutex.acquire();
			curRacers++;
		} catch (InterruptedException e1) {
			System.out.println("Problem with mutex");
		}
		// while the group is not full and all Racers have not been put into groups
		if(curRacers % main.getNumLines() != 0 && curRacers != main.getNumRacers()) {
			mutex.release();
			try {
				msg("Waiting for group to be built");
				group.acquire(); // add to group queue to build group
			} catch (InterruptedException e) {
				System.out.println("Problem waiting on group");
			}	
		}
		else { //if last in group or last Racer
			lastGroupLeft = main.getNumRacers() % main.getNumLines(); // number of Racers in last group
			msg("I'm last and building my group");
			mutex.release();
			for(int i = 0; i < main.getNumLines() - 1; i++) {
				group.release(); //pops off each racer in the group queue
			}
			ready.release(); //signals the judge that a group is ready
			if (curRacers == main.getNumRacers() && available.getQueueLength() == lastGroupLeft
					&& curRacers - numDone == lastGroupLeft)
				lastGroup = true; //for notifying Judge the last group has been formed
		}
		
		try {
			available.acquire(); //waits for availability in the river
		} catch (InterruptedException e1) {
			System.out.println("Problem acquiring available");
		} 
		
		try {
			// mutual exclusion over incrementing the number of Racers swimming/have swam past the river
			otherME.acquire();
			numDone++;
			otherME.release();
			sleep(randomNumber(4000,8000)); //crossing river
			msg("Swam across the river!");
		} catch (InterruptedException e1) {
			System.out.println("Problem crossing the river");
		} 
		
		msg("Letting the judge know I'm done.");
		done.release(); //each Racer notifies the Judge that they're finished crossing the river
		
		
		this.endRiverTime = System.currentTimeMillis() - time;
		this.riverTime = this.endRiverTime - this.startRiverTime;
		
		
		//Mutual exclusion over inputting values into the Judge's array for River times
		try {
			timeME.acquire();
		} catch (InterruptedException e2) {
			System.out.println("Problem getting mutex");
		}
		Judge.getRiverTime(this.getName(), this.riverTime);
		timeME.release();
		
		
		
		
		//GO HOME
		endTimeElapsed = System.currentTimeMillis()-time;
		timeElapsed = endTimeElapsed - startTimeElapsed;
		
		//Mutual exclusion over inputting values into the Judge's array for total race times
		try {
			mutex.acquire();
		} catch (InterruptedException e2) {
			System.out.println("Problem getting mutex");
		}
		Judge.getTimeElapsed(this.getName(), this.timeElapsed);
		mutex.release();
		
		
		try {
			otherME.acquire();
			finishLine.add(this); // adds each Racer to the finishLine queue to determine who is last
			otherME.release();
			msg("Waiting for my report.");
			report.acquire(); // wait for Judge to release reports
		} catch (InterruptedException e) {
			System.out.println("Problem waiting for report");
		}
		
		
		
		if (finishLine.size() == main.getNumRacers() && this.equals(finishLine.getLast())) { // if last Racer
			try {
				lastGoHome.acquire(); // wait for Judge to signal to go home
			} catch (InterruptedException e) {
				System.out.println("Problem with last going home");
			} 
			
			msg("Judge signaled me (the last Racer) to go home!");
			finishLine.remove(this); // remove last Runner from queue
			finishLine.getLast().goHome.release(); // signal friend to go home
		}
		
		else { // not last Racer
			
			try {
				msg("Waiting to go home");
				this.goHome.acquire(); // wait for friend to signal to go home
			} catch (InterruptedException e) {
				System.out.println("Problem acquiring goHome.");
			}
			
			if (finishLine.getFirst().equals(finishLine.getLast())) { //if last Racer left
				msg("I'm the last Racer left & I'm going home!");
				allGone.release(); // signal Judge that all have gone home
			}
			else  {
				msg("Telling my friend I'm going home");
				finishLine.remove(this); // remove Racer from queue
				finishLine.getLast().goHome.release(); // signal friend to go home
			}
		}	
			
		
	}			
}
