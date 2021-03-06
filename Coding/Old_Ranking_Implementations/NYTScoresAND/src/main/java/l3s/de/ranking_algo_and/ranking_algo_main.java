/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package l3s.de.ranking_algo_and;

/**
 *
 * @author vaibhav
 */
import java.io.FileNotFoundException;
import java.util.Map;
import l3s.de.relatedness.RelatednessScore;
import l3s.de.relativeness.RelativenessScore;
import l3s.de.timeliness.ImportanceScoreP;
import l3s.de.timeliness.TimelinessScore;

public class ranking_algo_main {   
    public static void main(String[] args) throws FileNotFoundException {
        
        //Entity of Interest E(Q)
        String entity = "http://dbpedia.org/resource/Nelson_Mandela";
        
        //Period of Interest P(Q)
        String fromDate = "1990-01-01";
        String toDate = "1990-03-31";

        //Get Importance Score for each Time Period P
        //For test case granularity set to Month
        ImportanceScoreP impscore = new ImportanceScoreP();
        Map<String, Double> impscore_map = impscore.imp_score_return(entity, fromDate, toDate);
        
        //Timeliness Score for each document d
        TimelinessScore timescore = new TimelinessScore();
        timescore.timeliness_score(entity, fromDate, toDate, impscore_map);
        
        //Relativeness Score for each document d
        RelativenessScore relscore = new RelativenessScore();
        relscore.relativeness_score(entity, fromDate, toDate);
        
        //Relatedness Score for each document d
        RelatednessScore relatedscore = new RelatednessScore();
        relatedscore.relatedness_score(entity, fromDate, toDate);
    }
}
        