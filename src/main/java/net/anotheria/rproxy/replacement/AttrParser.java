package net.anotheria.rproxy.replacement;

import java.util.LinkedList;
import java.util.List;

public class AttrParser {

    public static String addSubFolderToRelativePathesInSrcSets(String data, String sub) {
        List<String> links = new LinkedList<>();

        int c = 1;
        for (String d : data.split("srcset")) {

            if (c % 2 == 0) {
                //String temp = d;
                char[] ch = d.toCharArray();
                String content = null;
                for (int i = 0; i < ch.length; i++) {
                    char beginQuote = ch[i];
                    if (beginQuote == '"') {
                        for (int endQuoteIndex = i + 1; endQuoteIndex < ch.length; endQuoteIndex++) {
                            if (ch[endQuoteIndex] == '"') {
                                content = d.substring(i + 1, endQuoteIndex);
                                break;
                            }
                        }
                    }
                    if (content != null) {
                        break;
                    }
                }

                String[] linkArr = content.split(" ");
                for (String link : linkArr) {
                    if (link.startsWith("/")) {
                        links.add(sub + link);
                        data = data.replaceAll(link, sub + link);
                    }
                }

                System.out.println(links);

            }
            c++;
        }

        return data;
    }
}
