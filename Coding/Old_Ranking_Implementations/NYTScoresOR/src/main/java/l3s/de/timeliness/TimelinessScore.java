/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package l3s.de.timeliness;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import l3s.de.ranking_algo_or.VirtuosoConnector;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

/**
 *
 * @author vaibhav
 */
public class TimelinessScore{
    public void timeliness_score(String[] entityArray, String fromDate, String toDate, Map<String, Double> impscore_map, String timePeriod, int counter) throws FileNotFoundException{
        
        TimelinessScore timeScore = new TimelinessScore();
        
        //Getting the articles and the corresponding time period P in which they were published 
        ParameterizedSparqlString articlesByP = new ParameterizedSparqlString("PREFIX oa: <http://www.w3.org/ns/oa#>\n" +
                                                                              "PREFIX oae: <http://www.ics.forth.gr/isl/oae/core#>\n" +
                                                                              "PREFIX dc: <http://purl.org/dc/terms/>\n" + 
                                                                              "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                                                                              "PREFIX schema:  <http://schema.org/>\n" +
                                                                              "SELECT distinct ?article ("+timePeriod+"(?date) as ?"+timePeriod+")  WHERE {\n" +
                                                                              "{ \n" +
                                                                              "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') &&\n" +
                                                                              "?date <= xsd:date('" + toDate + "')).\n" +
                                                                              "?article schema:mentions ?entity .\n" +
                                                                              "?entity oae:hasMatchedURI  <" + entityArray[0] + ">.\n" +
                                                                              "} \n"
                                                                              );
        
        for(int i = 1; i< entityArray.length; i++){
            ParameterizedSparqlString articlesByP_toAppend = new ParameterizedSparqlString("UNION { \n " +
                                                                                           "?article schema:mentions ?entity"+i+" .\n" +
                                                                                           "?entity"+i+" oae:hasMatchedURI  <" + entityArray[i] + "> .\n" +
                                                                                           "?article dc:date ?date FILTER(?date >= xsd:date('" + fromDate + "') &&\n" +
                                                                                           "?date <= xsd:date('" + toDate + "')).\n"+
                                                                                           "}"); 
            articlesByP.append(articlesByP_toAppend);
        }
        
        ParameterizedSparqlString articlesByP_endAppend = new ParameterizedSparqlString("}");
        
        articlesByP.append(articlesByP_endAppend);
        
        VirtuosoConnector connec = new VirtuosoConnector();
        VirtGraph graph = new VirtGraph(connec.getGraph(), connec.getHost(), connec.getUsername(), connec.getPwd());
        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(articlesByP.toString(), graph);
        ResultSet results = vqe.execSelect();
        String totaldocsxml = ResultSetFormatter.asXMLString(results);
        Document doc = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = loadXMLFromString(totaldocsxml);
        } catch (Exception e) {
            System.out.println("Error in parsing ArticlesByPeriod XML");
        }
        Map<String, Double> Article_TimeScore_Map = new LinkedHashMap<>();
        NodeList nList = doc.getElementsByTagName("result");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String article = eElement.getElementsByTagName("binding").item(0).getTextContent();
                article = article.replaceAll("\n", "").replaceAll(" ", "");
                String timegranularity = eElement.getElementsByTagName("binding").item(1).getTextContent();
                timegranularity = timegranularity.replaceAll("\n", "").replaceAll(" ", "");
                double timelinessScore = impscore_map.get(timegranularity);
                Article_TimeScore_Map.put(article, timelinessScore);
            }
        }
        
        Map<String, Double> Ranked_Article_TimeScore_Map = timeScore.sortHashMapByValues(Article_TimeScore_Map);
        
        //Printing each article and the timelinessScore in a csv file
        PrintWriter writer = new PrintWriter("./ranking_timeliness"+counter+".csv");
	writer.println("Article; TimelinessScore");
        
        for(Map.Entry<String, Double> entry: Ranked_Article_TimeScore_Map.entrySet()){
            String article = entry.getKey();
            System.out.println("Article "+ article);
            Double timelinessScore = entry.getValue();
            System.out.println("TimelinessScore "+ timelinessScore);
            writer.println(article + "; "+ timelinessScore);
        }
        
        writer.close();
        
    }
    
    protected static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
    
    protected String getString(String tagName, Element element) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes();

            if (subList != null && subList.getLength() > 0) {
                return subList.item(0).getNodeValue();
            }
        }
        return null;
    }
    
    public static LinkedHashMap<String, Double> sortHashMapByValues(Map<String, Double> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues, Collections.reverseOrder());
        Collections.sort(mapKeys, Collections.reverseOrder());

        LinkedHashMap<String, Double> sortedMap
                = new LinkedHashMap<>();

        Iterator<Double> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Double val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Double comp1 = passedMap.get(key);
                Double comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }
}
