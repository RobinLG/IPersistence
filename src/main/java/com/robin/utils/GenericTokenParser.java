package com.robin.utils;

import sun.jvm.hotspot.oops.BooleanField;

/**
 * @author Robin
 */
public class GenericTokenParser {

    private final String openToken; // sign of starting
    private final String closeToken; // sign of ending
    private final TokenHandler tokenHandler; //  executor of sign

    public GenericTokenParser(String openToken, String closeToken, TokenHandler tokenHandler) {
        this.openToken = openToken;
        this.closeToken = closeToken;
        this.tokenHandler = tokenHandler;
    }

    /**
     * Parsing ${} and #{}.
     * This method implemented work of parsing and executing for placeHolder in configuration file and script.
     * This method for work of parsing.
     * handleToken() of executor handler for work of executing.
     * @param text
     * @return
     */
    public String parse(String text) {
        // if null, return ""
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Verifying whether including sign of start.
        // if not, the default is not a placeHolder, and return original text.
        // else continue to execute.
        int start = text.indexOf(openToken, 0);
        if (start == -1) {
            return text;
        }

        // Transforming text to byte[], and defining the fault offset.
        // Storing variable 'builder' of String that need to be return.
        // expression is the variable mapping the placeHolder in variable 'text'
        // Judge 'start' whether greater than -1.(Judge is there openToken in text).
        // if exist, execute the code below.
        char[] src = text.toCharArray();
        int offset = 0;
        final StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        while (start > -1) {
            // if there is an escape character before start signing, will not handle 'openToken'.
            // else continue to handle.
            if (start > 0 && src[start - 1] == '\\') {
                builder.append(src, offset, start - offset - 1).append(openToken);
                offset = start + openToken.length();
            } else {
                // resetting 'expression' for avoiding null pointer or old data interference.
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }
                builder.append(src, offset, start - offset);
                offset = start + openToken.length();
                int end = text.indexOf(closeToken, offset);
                while (end > -1) { // when there is sign of ending.
                    if (end > offset && src[end - 1] == '\\') { // when there is escape character before sign of ending.
                        // this close token is escaped. remove the backslash and continue.
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        end = text.indexOf(closeToken, offset);
                    } else {
                        expression.append(src, offset, end - offset);
                        offset = end + closeToken.length();
                        break;
                    }
                }
                if (end == -1) {
                    // close token was not found
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                } else {
                    // Handling parameter according their key(that is expression).
                    // Returning ? as placeHolder
                    builder.append(tokenHandler.handleToken(expression.toString()));
                    offset = end + closeToken.length();
                }
            }
            start = text.indexOf(openToken, offset);
        }
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
    }
}
