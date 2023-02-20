package com.example.fractalrangecalculator.Util;



import com.example.fractalrangecalculator.Model.Bar;

import java.util.ArrayList;
import java.util.List;

public class ListSplitter {

    public static ArrayList<List<Bar>> splitBar(List<Bar> fulllist, int numOfOutputs) {
        // get size of the list
        int size = fulllist.size();
        ArrayList<List<Bar>> lists = new ArrayList<>();
        int lastDivider = 0;
        for(int i = 0;i < numOfOutputs; i++){
            int newDivider = (int) Math.round(size*((i+1)/(double)numOfOutputs));
            lists.add(new ArrayList<>(fulllist.subList(lastDivider, newDivider)));
            lastDivider = newDivider;
        }
        // return an List array to accommodate both lists
        return lists;
    }
}
