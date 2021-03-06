/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package l3s.de.ranking_cat;

/**
 *
 *  @author Vaibhav Kasturia <kasturia at l3s.de>
 * 
 * The main class: Reads inputs from file and passes input to different files for ranking 
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import l3s.de.pagerank.GraphCreator;
import l3s.de.relativeness.RelativenessScore;
import l3s.de.timeliness.ImportanceScoreP;
import l3s.de.timeliness.TimelinessScore;

public class ranking_algo_main {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        //File in which the query parameters are stored 
        String file = "./query_param";
 
        int counter = 1;
        
        //Read file
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"))) {
            String str;
            while ((str = in.readLine()) != null) {
                //System.out.println(str);
                if (str.startsWith("#")) {
                    counter += 1;
                    continue;
                }

                String[] data = str.split("\t");
                
                String Category = data[0].trim();
                
                if (data.length != 4) {
                    System.out.println("*** Malformed line! Continuing to next line...");
                }
            
                //Period of Interest P(Q)
                String fromDate = data[1].trim();
                String toDate = data[2].trim();
                String timePeriod = data[3].trim();
                
                //Getting the Entities of Interest from the Category
                Cat_to_entities categoryToEntities = new Cat_to_entities();
                String[] entityArray = categoryToEntities.entitiesreturn(Category, fromDate, toDate);
                
                //Get Importance Score for each Time Period P
                ImportanceScoreP impscore = new ImportanceScoreP();
                Map<String, Double> impscore_map = impscore.imp_score_return(entityArray, fromDate, toDate, timePeriod);

                //Timeliness Score for each document d
                TimelinessScore timescore = new TimelinessScore();
                Map<String, Double> article_timelinessScore_map = timescore.timeliness_score(entityArray, fromDate, toDate, impscore_map, timePeriod, counter);

                //Relativeness Score for each document d
                RelativenessScore relscore = new RelativenessScore();
                Map<String, Double> article_relativenessScore_map = relscore.relativeness_score(entityArray, fromDate, toDate, counter);

                
                //Combination of timeliness and relativeness score for each document d
                ScoreCombos combined = new ScoreCombos();
                Map<String, Double> article_time_relativeness_map = combined.scoreCombination(article_timelinessScore_map, article_relativenessScore_map, counter); 
                
                //Map of all query documents and their extracted entities 
                Test_Docs_and_Entities test_docs_entities = new Test_Docs_and_Entities();
                Map<String, Map<String, Double>> test_docs_entities_map = test_docs_entities.docs_entities_return(entityArray, fromDate, toDate);
                
                //Create the transition Graph to run the algorithm
                GraphCreator graphCreator = new GraphCreator();
                graphCreator.createGraph(test_docs_entities_map, article_time_relativeness_map, entityArray, counter);
                
                counter+=1;
            }
        }
    }
}     