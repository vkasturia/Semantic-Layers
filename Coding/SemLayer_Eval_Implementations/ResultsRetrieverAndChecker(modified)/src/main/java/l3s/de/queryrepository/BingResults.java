/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resultsretrieverandcheckerlocal;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Fafalios
 */
public class BingResults {

    private ArrayList<Result> results;

    public BingResults(String query, int numOfResults) throws ParserConfigurationException, SAXException, IOException, ParseException {

        results = new ArrayList<>();
        int numOfRequests = numOfResults / 50;

        for (int x = 0; x < numOfRequests; x++) {

            int offset = 50 * x;

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("http://www.bing.com/search?q=");
            queryBuilder.append(query.replaceAll(" ", "%20"));
            queryBuilder.append("%20site:nytimes.com&count=50&first=").append(offset).append("&format=rss");
            String urlStr = queryBuilder.toString();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(urlStr);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("item");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String link = eElement.getElementsByTagName("link").item(0).getTextContent();
                    String title = eElement.getElementsByTagName("title").item(0).getTextContent();
                    String description = eElement.getElementsByTagName("description").item(0).getTextContent();
                    String publicationDate = eElement.getElementsByTagName("pubDate").item(0).getTextContent();
                    String formattedPubDate = BingResults.dateFormatter(publicationDate);

                    Result result = new Result(link, title, description, formattedPubDate);
                    results.add(result);
                }
            }
        }
    }

    public static String parseDate(String date) {

        int i1 = date.indexOf(",");
        String part = date.substring(i1 + 2).replace("GMT", "").trim();
        part = part.substring(0, part.lastIndexOf(" ")).trim();

        //Change German Format months to English Format months
        part = part.replace("Mrz", "Mar");
        part = part.replace("Mai", "May");
        part = part.replace("Okt", "Oct");
        part = part.replace("Dez", "Dec");

        part = part.replace("Jan", "01");
        part = part.replace("Feb", "02");
        part = part.replace("Mar", "03");
        part = part.replace("Apr", "04");
        part = part.replace("May", "05");
        part = part.replace("Jun", "06");
        part = part.replace("Jul", "07");
        part = part.replace("Aug", "08");
        part = part.replace("Sep", "09");
        part = part.replace("Oct", "10");
        part = part.replace("Nov", "11");
        part = part.replace("Dec", "12");

        return part;
    }

    public static String dateFormatter(String pubDate) throws ParseException {

        String parsedDate = BingResults.parseDate(pubDate);
        DateFormat formatter = new SimpleDateFormat("d MM yyyy");
        Date date = new Date(formatter.parse(parsedDate).getTime());

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String formattedDate = cal.get(Calendar.DATE) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.YEAR);
        //System.out.println("formattedDate : " + formattedDate);
        return formattedDate;

    }

    public ArrayList<Result> getResults() {
        return results;
    }

    public void setResults(ArrayList<Result> results) {
        this.results = results;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

}
