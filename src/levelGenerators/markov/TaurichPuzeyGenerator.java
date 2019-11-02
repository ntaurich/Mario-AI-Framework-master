package levelGenerators.markov;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.ArrayList;
import java.util.*;

//
public class TaurichPuzeyGenerator implements MarioLevelGenerator {
    private final float GAP_PROB = 0.1f;
    private final float Prob = 0.1f;
    Random rnd = new Random();
    String levels[]= {"---x","o--x","o--x","o--x","--xx","x--x","x--x"};

    public static String getLevel(String filepath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
        }
        return content;
    }

    //function that takes in a level and returns an array with all the columns in it
    public String[] levelToColumn(String level){
        //the full level as an array of characters
        char[] unsanatizedArray = level.toCharArray();

        //Make a new string
        //Copy over everything except the carriage returns into a string
        //make that string into an array
        String sanatizeString = "";
        for(int i = 0; i < unsanatizedArray.length; i++){
            if(unsanatizedArray[i] != '\r'){
                sanatizeString += unsanatizedArray[i];
            }
        }
        char[] array = sanatizeString.toCharArray();

        //Finds the width of the level
        int width = 0;
        for(int i = 0; i < array.length; i++){
            if(array[i]== '\n'){
                width = i + 1;
                if((i + 1 < array.length - 1) && array[i+1] == '\r')
                    width = i+2;
                break;
            }
        }
        //Makes an empty string array
        String columnArray[]= new String[width];
        for(int c = 0; c < width; c++){
            columnArray[c]= "";
        }
        //Goes through the level and makes a new array where the column is each element
        for(int characterIndex = 0; characterIndex < array.length; characterIndex++){
            System.out.println(characterIndex + ": " + array[characterIndex] + " into array number " + characterIndex % width);
            columnArray[(characterIndex % width)] += array[characterIndex];
            System.out.println(columnArray[characterIndex % width]);
        }
        //returns an array  with each column of the level
        return columnArray;
    }


    public String[] makeStrings(char[][] level){
        String array[]= new String[level.length];
        for (int i =0; i<level.length; i++){
            array[i]= "";
            for(int j=0; j<level[0].length; j++){
                array[i]+= level[i][j];
            }
        }
        return array;
    }

    public List<Object> makeTable(String[] level){
        ArrayList<String> slices =new ArrayList<String>();//Creating arraylist that holds found slices
        ArrayList<Integer> totals =new ArrayList<Integer>();//Creating arraylist that holds totals of each slice type
        int numSlices =0; //number of different slices found
        //make double array of integers
        ArrayList<ArrayList<Integer>> graph = new ArrayList<>();
        graph.add(new ArrayList());
        slices.add(level[0]);
        totals.add(0);
        graph.get(0).add(0);
        numSlices=1;
        int prevSlice=0;
        for (int i=0; i<level.length-1;i++){ //for each section
            boolean found= false; //if slice has been seen before
            //look at next slice and compare to found slices
            for (int j=0; j<numSlices; j++){
                if (slices.get(j).equals(level[i+1])){ //slice next exists already
                    graph.get(prevSlice).set(j, graph.get(prevSlice).get(j) + 1);
                    //add 1 to j column of current row
                    totals.set(prevSlice, totals.get(prevSlice)+1); //update totals
                    found= true;
                    prevSlice=j;
                    break;
                }
            }
            if (found){
                continue;
            }
            //if new slice found
            slices.add(level[i+1]);
            numSlices++;
            graph.add(new ArrayList<>(numSlices));
            for(int a=0; a<numSlices; a++){// add a extra spaces to table
                for(int b=graph.get(a).size(); b<numSlices; b++) {
                    graph.get(a).add(0);
                }
            }
            //add new lines to table
            graph.get(prevSlice).set(numSlices-1, 1); //add 1 to last column of current row
            totals.add(0);
            totals.set(prevSlice,totals.get(prevSlice)+1);
            prevSlice= numSlices-1;
        }
        float probs[][]= new float[numSlices][numSlices];

        //print graph and put probabilities in table
        for (int i=0; i<numSlices; i++){
            for (int j=0; j<numSlices; j++){
                System.out.print(graph.get(i).get(j));
                int total= totals.get(i);
                int curr= graph.get(i).get(j);
                probs[i][j]= (float) curr/total;
            }
            System.out.println();
        }

        //print totals
        for (int a=0; a< totals.size(); a++){
            System.out.print(totals.get(a));
        }

        return Arrays.asList(probs,slices);


        //return probs;

    }



    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {
        //TODO: Get it to take a random level
        //TODO: Fix the probability to take in the levels
        String level = getLevel("levels/original/lvl-1.txt");


        int btm = model.getHeight()-1;
        int end = model.getWidth() -1;
        String[] columnArray= levelToColumn(level);


        List<Object> values = makeTable(columnArray);
        float[][] probs= (float[][]) values.get(0);
        ArrayList<String> slices = (ArrayList<String>) values.get(1);
        System.out.println();
        for(int i=0; i<probs.length; i++){
            for (int j=0; j<probs.length; j++){
                System.out.print(probs[i][j]);
                System.out.print("|");
            }
            System.out.println();
        }

        for (int a =0; a<slices.size(); a++){
            System.out.println(slices.get(a));
        }

        for(int x =0; x<model.getWidth();x++){
            float num =  this.rnd.nextFloat();

            //gap generation
            if(num<GAP_PROB){
                model.setBlock(x, btm, MarioLevelModel.EMPTY);
                model.setBlock(x, btm-1, MarioLevelModel.EMPTY);
            }
            //floor generation
            else {
                model.setBlock(x, btm, MarioLevelModel.GROUND);
                model.setBlock(x, btm - 1, MarioLevelModel.GROUND);
            }
        }
        model.setBlock(end, btm-2,MarioLevelModel.MARIO_EXIT);
        return model.getMap();
    }

    @Override
    public String getGeneratorName() {
        return "TaurichPuzeyGenerator";
    }
}
