package geneticlsystems;

import java.io.IOException;
import java.util.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;

import com.amazonaws.regions.Regions;
import com.amazonaws.regions.Region;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

public class Initialize implements RequestHandler<InitializeRequest, InitializeResponse> {

    public static LSystem initialize(){
        //generates a random individual
        //initialize angle
        Random r = new Random();
        int angle = r.nextInt(360);
        while(angle<30 || angle > 330 || (angle >150 && angle < 210)){
            angle = r.nextInt(360);
        }

        //initialize axiom
        String axiom = "";
        String letters [] = {"X", "Y", "Z", "C"};
        String symbols [] = {"+", "-", "[", "]"};
        axiom += letters[r.nextInt(letters.length-1)];
        int len = r.nextInt(5);
        for(int i=0; i<len; i++){
            if(r.nextBoolean()){
                //letters
                axiom+=letters[r.nextInt(letters.length-1)];
            }
            else{
                //symbol
                axiom+=symbols[r.nextInt(symbols.length-2)];
            }
        }

        //initialize rules
        String rules [] = new String [letters.length-1];
        for(int s =0; s<letters.length-1; s++){
            String rule = "";
            rule+=letters[s];
            rule+="=";
            len = r.nextInt(7)+1;
            for(int i=0; i<len; i++){
                if(r.nextBoolean()){
                    //letter
                    int index;
                    if(r.nextDouble()<0.6){
                        index = r.nextInt(letters.length-1);
                    }
                    else{
                        index = r.nextInt(letters.length);
                    }
                    if(letters[index].equals("C")){
                        rule+="C"+(r.nextInt(10)+"");
                    }
                    else {
                        rule += letters[index];
                    }
                }
                else{
                    //symbol
                    int index;
                    if(r.nextDouble()<0.7){
                        index=r.nextInt(symbols.length-2);
                    }
                    else{
                        index = r.nextInt(symbols.length);
                    }
                    rule+=symbols[index];
                }
            }
            rules[s] = rule;
        }

        //initialize rank
        int rank = r.nextInt(91)+10;

        return new LSystem(angle, 5, axiom, rules, "", rank);
    }

    @Override
    public InitializeResponse handleRequest(InitializeRequest input, Context context) {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(Regions.EU_WEST_1));
        DynamoDB dynamoDB = new DynamoDB(client);

        for(int i=0; i<20; i++) {
            //initialize
            LSystem l = initialize();
            l.setId( UUID.randomUUID().toString() );

            // save to dynamodb
            LSystem.saveLSystem(l, dynamoDB);

            String g_commands = l.getAngle() + ":" +  LSystem.expand(l);
            // write to s3
            LSystem.uploadToS3(l.getId(), g_commands);
        }

        return new InitializeResponse("");
    }
}
