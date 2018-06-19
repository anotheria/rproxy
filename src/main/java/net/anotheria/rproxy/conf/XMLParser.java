package net.anotheria.rproxy.conf;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Get configuration from xml.
 */
public class XMLParser {

    private static final List<String> tgNames = new LinkedList<>();

    {
        tgNames.add("replace");
    }

    private XMLParser() {

    }

    public List<ContentReplace> parseConfig(String fileName, List<String> tagNames) {
        try {
            File fXmlFile = getFile(fileName);
            if (fXmlFile == null) {
                return null;
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();
            List<ContentReplace> rules = new LinkedList<>();

            for (String tagName : tagNames) {
                NodeList nList = doc.getElementsByTagName(tagName);


                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String type = eElement.getAttribute("type");
                        switch (type) {
                            case "relative":
                                String to = eElement.getElementsByTagName("URLtoReplace").item(0).getTextContent();
                                String with = eElement.getElementsByTagName("URLreplaceWith").item(0).getTextContent();

//                                System.out.println("toRep : " + to);
//                                System.out.println("withRep : " + with);
                                rules.add(new ContentReplaceRelative(to, with));
                                break;
                        }
                    }
                }
            }

            return rules;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String argv[]) {
        XMLParser p = new XMLParser();
        List<ContentReplace> r = p.parseConfig("conf1.xml", tgNames);
        String link = "/faq/wp-content/uploads/2018/04/TCL_logo_black_pink.png";
        String data = link + " somethin else";
        System.out.println(data);
        for (ContentReplace c : r) {
            ContentReplaceRelative cr = (ContentReplaceRelative) c;
            // System.out.println(cr.toString());
            data = c.applyReplacement(data);
        }
        System.out.println(data);
    }

    private File getFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }

    public static List<String> getTgNames() {
        return tgNames;
    }

}