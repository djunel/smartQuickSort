import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

public class smartQuickSort {


    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    /* define constants */
    static long MAXVALUE =  2000000000;
    static long MINVALUE = -2000000000;
    static int numberOfTrials = 100;
    static int MAXINPUTSIZE  = (int) Math.pow(1.5,35);
    static int MININPUTSIZE  =  1;
    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time

    static String ResultsFolderPath = "/home/diana/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;


    public static void main(String[] args) {
        //function to verify it is sorting correctly

        checkSortCorrectness();

        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized

        System.out.println("Running first full experiment...");
        runFullExperiment("SmartQuickSort-Exp1-ThrowAway.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("SmartQuickSort-Exp2.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("SmartQuickSort-Exp3.txt");
    }

    static void runFullExperiment(String resultsFileName){
        //declare variables for doubling ratio
        double[] averageArray = new double[1000];
        double currentAv = 0;
        double doublingTotal = 0;
        int x = 0;

        //set up print to file
        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch(Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file "+ResultsFolderPath+resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        //declare variables for stop watch
        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        //add headers to text file
        resultsWriter.println("#InputSize    AverageTime    DoublingRatio"); // # marks a comment in gnuplot data
        resultsWriter.flush();

        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for(int inputSize=MININPUTSIZE;inputSize<=MAXINPUTSIZE; inputSize*=2) {

            // progress message...
            System.out.println("Running test for input size "+inputSize+" ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of randomly spaced integers in ascending sorted order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            // but we will randomly generate the search key for each trial
            System.out.print("    Generating test data...");

            //generate random integer list
            long[] testList = createRandomIntegerList(inputSize);

            //print progress to screen
            System.out.println("...done.");
            System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();


            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the trials
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // generate a random key to search in the range of a the min/max numbers in the list
                //long testSearchKey = (long) (0 + Math.random() * (testList[testList.length-1]));
                /* force garbage collection before each trial run so it is not included in the time */
                // System.gc();

                //TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                //long foundIndex = MergeSort(testList);
                long[] SortedList = QuickSortAlg(testList);
                // batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double)numberOfTrials; // calculate the average time per trial in this batch

            //put current average time in array of average times. We will be able to use this to calculate the doubling ratio
            averageArray[x] = averageTimePerTrialInBatch;

            //skip this round if this is the first one (no previous average for calculation)
            if(inputSize != 1){
                doublingTotal = averageTimePerTrialInBatch/averageArray[x-1]; //Calculate doubling ratio

            }
            x++;
            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f %15.2f \n",inputSize, averageTimePerTrialInBatch, doublingTotal); // might as well make the columns look
            /*resultsWriter.printf("%12d  %15.2f \n",inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice*/
            resultsWriter.flush();
            System.out.println(" ....done.");
        }
    }

    /*Verify merge sort is working*/
    static boolean verifySort(long[] list){

        boolean sorted = true;
        int i = 0;
        //loop through list
        for(i = 1; i < list.length; i++) {
            //set the current number to number on right side of the pair of numbers you are checking
            long currentNum = list[i];
            //set j equal to the left number of the pair of numbers you are checking
            int j = i - 1;
            //check if the left side number is greater than the one on the right.
            //if it is, move it to the right decrement j. Keep doing this until j is less than 0
            while (j >= 0 && list[j] > currentNum) {
                list[j + 1] = list[j];
                j = j - 1;
                sorted = false;
            }
            //set the far left number equal to the current number.
            list[j + 1] = currentNum;
        }
        return sorted;
    }

    static void checkSortCorrectness(){
        //test to sort small list - print before and after sort
        long[] testList1 = createRandomIntegerList(15);
        //long[] testList1 = new long[]{1,2, -3, 5,3,9,12,55,-22,-13,-5,10,20,23,-2,4};


        System.out.println("Small list test: " );
        System.out.println("Array unsorted: " + Arrays.toString(testList1));
        long[] resultList = QuickSortAlg(testList1);
        System.out.println("Array sorted: " + Arrays.toString(resultList));

        //test to sort medium list - verifySort through function
        //and return true if sorted, false if not sorted
        long[] testList2 = createRandomIntegerList(200);
        long[] resultList2 = QuickSortAlg(testList2);
        boolean sorted2 = verifySort(resultList2);
        System.out.println("Medium list test sorted: " + sorted2);

        //test to sort large list - verifySort through function
        //and return true if sorted, false if not sorted
        long[] testList3 = createRandomIntegerList(1000);
        long[] resultList3 = QuickSortAlg(testList2);
        boolean sorted3 = verifySort(resultList2);
        System.out.println("Large list test sorted: " + sorted3);

    }


        public static long[] QuickSortAlg(long[] list) {


                list = QuickSortWorker(list, 0, list.length-1);


                return list;
        }

        public static long[] QuickSortWorker(long[] list, int lo, int high){

            if(lo >= high){
                return list;
            }
            else{
                int pivotIndex = lo + (high - lo)/2;
                long tempV = list[lo];
                list[lo] = list[pivotIndex];
                list[pivotIndex] = tempV;
                pivotIndex = lo;
                int nextHi = high;

                while(pivotIndex < nextHi){
                    if(list[pivotIndex + 1] <= list[pivotIndex]){
                        long tempV2 = list[pivotIndex + 1];
                        list[pivotIndex + 1] = list[pivotIndex];
                        list[pivotIndex] = tempV2;
                        pivotIndex++;
                    }
                    else{

                        long tempV3 = list[pivotIndex + 1];
                        list[pivotIndex + 1] = list[nextHi];
                        list[nextHi] = tempV3;

                        nextHi--;
                    }
                }
                QuickSortWorker(list, lo, pivotIndex-1);
                QuickSortWorker(list, nextHi + 1, high);
            }

            return list;
        }


        public static long[] createRandomIntegerList(int size) {

            long[] newList = new long[size];
            for (int j = 0; j < size; j++) {
                newList[j] = (long) (MINVALUE + Math.random() * (MAXVALUE - MINVALUE));
            }
            return newList;
        }


}
