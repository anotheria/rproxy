package net.anotheria.rproxy.replacement;

public class AttrParser {
    private AttrParser(){

    }

    /**
     * Adds subdomain in relative paths for each link in srcsets.
     * @param data html document
     * @param sub subdomain (subfolder)
     * @return html document with replacement
     */
    public static String addSubFolderToRelativePathesInSrcSets(String data, String sub) {
        int c = 1;
        for (String d : data.split("srcset")) {
            if (c % 2 == 0) {
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
                //System.out.println(Arrays.toString(linkArr));
                for (String link : linkArr) {
                    if (link.startsWith("/") && !link.startsWith("/" + sub)) {
                        data = data.replaceAll(link, sub + link);
                    }
                }
            }
            c++;
        }
        return data;
    }
}
