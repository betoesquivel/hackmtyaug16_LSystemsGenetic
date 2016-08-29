package geneticlsystems;

/**
 * Created by enrique on 29/08/16.
 */

import java.util.*;
import java.io.IOException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;

import com.amazonaws.regions.Regions;
import com.amazonaws.regions.Region;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

public class Mutate implements RequestHandler<Request, Response>{

    @Override
    public MutateResponse handleRequest(MutateRequest input, Context context) throws IOException{

        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(Regions.EU_WEST_1));
        DynamoDB dynamoDB = new DynamoDB(client);

        double mutationChance = 0.1;

        LSystem parent = input.getParent();
        String letters [] = {"X", "Y", "Z", "C"};
        String symbols [] = {"+", "-", "[", "]"};

        //mutate angle
        Random r = new Random();
        int angle = parent.getAngle();
        String angleStr = Integer.toBinaryString(angle);
        while(angleStr.length() < 9){
            angleStr = "0"+angleStr;
        }
        String newAngle = "";
        for(int i =0; i<angleStr.length(); i++){
            if(r.nextDouble()<mutationChance){
                if(angleStr.charAt(i) == '0'){
                    newAngle +="1";
                }
                else{
                    newAngle +="0";
                }
            }
            else{
                newAngle+=angleStr.substring(i, i+1);
            }
        }

        //mutate iterations
        int iterations = parent.getIterations();
        int newIterations = iterations;

        //mutate axiom
        String axiom = parent.getAxiom();
        String newAxiom = "";
        for(int i=0; i<axiom.length(); i++) {
            if(r.nextDouble()<mutationChance){
                //mutate character
                if(r.nextBoolean())
                    newAxiom+=letters[r.nextInt(letters.length-1)];
                else
                    newAxiom+=symbols[r.nextInt(symbols.length-2)];
            }
            else{
                newAxiom += axiom.substring(i, i+1);
            }
        }

        //check that axiom has a letter, if not: add one at the beginning
        boolean foundLetter = false;
        for(int i =0; i<letters.length; i++){
            if(newAxiom.contains(letters[i])){
                foundLetter = true;
            }
        }
        if(!foundLetter){
            newAxiom+=letters[r.nextInt(letters.length-1)];
        }

        //mutate rules
        String rules [] = parent.getRules();
        String newRules [] = new String [rules.length];
        for(int i=0; i<rules.length; i++){
            newRules[i] = rules[i].substring(0,2);
            for(int j=2; j<rules[i].length(); j++){
                if(r.nextDouble()<mutationChance){
                    //mutate character

                    if(rules[i].charAt(j)=='C'){
                        //mutate the number
                        newRules[i]+="C"+(r.nextInt(9)+"");
                        j++;
                        continue;
                    }

                    if(r.nextBoolean()){
                        newRules[i]+=letters[r.nextInt(letters.length)];
                    }
                    else{
                        newRules[i]+=symbols[r.nextInt(symbols.length)];
                    }
                }
                else{
                    newRules[i]+=rules[i].substring(j, j+1);
                }
                if(r.nextDouble()<(mutationChance*2.0)){
                    j--;
                }
                if(r.nextDouble()<(mutationChance*2.0)){
                    j++;
                }
            }
        }

        //mutated individual constants
        String constants = parent.getConstants();
        String newConstants = constants;

        LSystem offspring = new LSystem(
          Integer.parseInt(newAngle,2),
          newIterations,
          newAxiom,
          newRules,
          newConstants,
          -10
        );
        String id = UUID.randomUUID().toString();
        offspring.setId(id);

        //expand
        String g_commands = offspring.getAngle() + ":" + LSystem.expand(offspring);

        //save in dynamo
        LSystem.saveLSystem( offspring, dynamoDB );

        //save in S3
        LSystem.uploadToS3( offspring.getId(), g_commands );

        return new MutateResponse(id);
    }
}
