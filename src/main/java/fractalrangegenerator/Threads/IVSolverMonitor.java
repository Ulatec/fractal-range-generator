package com.example.fractalrangecalculator.Threads;



import com.example.fractalrangecalculator.Model.Bar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IVSolverMonitor{

    int numberOfThreads;
    int finishedThreads;
    List<Bar> barList ;

    int originalListSize;
    private static Logger logger = LogManager.getLogger(IVSolverMonitor.class);
    HashMap<Integer, List<Bar>> barMap = new HashMap<>();

    boolean safeToKill = false;
    public IVSolverMonitor(int numberOfThreads, int originalListSize){
        this.numberOfThreads = numberOfThreads;
        this.originalListSize = originalListSize;
        barList = new ArrayList<>();
        finishedThreads = 0;
    }
    public void run(){
        while(!safeToKill) {
            //DO NOTHING
        }
    }

    public synchronized void threadFinished(int threadNum, List<Bar> barList){
        this.barList.addAll(barList);
        barMap.put(threadNum, barList);
        finishedThreads++;
        if(finishedThreads == numberOfThreads){
            logger.info("Finished calculating all implied volatility for quest.");
            safeToKill = true;
        }
    }

    public synchronized List<Bar> getImpliedVolailityCalculationResults(){
        if(barMap.entrySet().size()<numberOfThreads){
            return null;
        }else{
            safeToKill = true;
            return barList;
        }
    }

}
