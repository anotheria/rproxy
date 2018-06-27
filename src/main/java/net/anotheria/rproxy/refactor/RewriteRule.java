package net.anotheria.rproxy.refactor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RewriteRule {
    private Pattern sourePattern;
    private Pattern targetPattern;
    private Matcher matcher;
    private RewriteType rewriteType;
}
