package com.devoteam.srit.xmlloader.core.coding.text;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.PluggableParameterOperatorSetFromSDP;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.util.HashMap;

public class ContentParser {

    private String[] multipartArray = null;
    private String content = null;

    // --- construct --- //
    public ContentParser(String protocol, String content, String contentBoundary) throws Exception {
        this.content = content;
        multipartArray = contentSDPPart(protocol, content, contentBoundary);
    }

    public void addContentParameter(Parameter var, String[] params, String path) throws Exception {
        if (params.length == 1 && params[0].toLowerCase().startsWith("content")) {
            try {
                // case no content
                if (content == null || content.length() == 0) {
                    return;
                }

                // case no index in the path => the entire content
                int posBegin = params[0].indexOf("(");
                int posEnd = params[0].indexOf(")");
                if ((posBegin < 0) || (posEnd < 0)) {
                    var.add(content);
                    return;
                }
                int part = Integer.valueOf(params[0].substring(posBegin + 1, posEnd));
                var.add(multipartArray[part]);
            } catch (Exception e) {
                throw new ExecutionException("Error in content content: " + e.getMessage());
            }
        } else if (params.length == 2 && params[0].toLowerCase().startsWith("content")) {
            if (multipartArray != null) {
                // case an index in the path => the specific content
                int posBegin = params[0].indexOf("(");
                int posEnd = params[0].indexOf(")");
                if ((posBegin >= 0) && (posEnd >= 0)) {
                    int part = Integer.valueOf(params[0].substring(posBegin + 1, posEnd));
                    content = multipartArray[part].trim();
                }
            }
            //---------------------------------------------------------------------- content(X):Type -
            if (params[1].equalsIgnoreCase("Type")) {
                try {
                    if ((content != null) && multipartArray != null && multipartArray.length > 1) {
                        var.add(content.substring(0, content.indexOf("\r\n")));
                    }
                } catch (Exception e) {
                    throw new ExecutionException("Error in content content:Type : " + e.getMessage());
                }
            } //---------------------------------------------------------------------- content(X):Sdp -
            else if (params[1].equalsIgnoreCase("Sdp")) {
                try {
                    if (content != null) {
                        var.add(content.substring(content.indexOf("\r\n\r\n") + 1, content.length()));
                    } else {
                        String sdpContent = content;
                        sdpContent = content.substring(content.indexOf("\r\n\r\n") + 1, content.length());
                        var.add(sdpContent);
                    }
                } catch (Exception e) {
                    throw new ExecutionException("Error in content:Sdp : " + e.getMessage());
                }
            }
        } else if (params.length > 2 && params[1].equalsIgnoreCase("Sdp")) {
            String sdpContent = content;
            // case an index in the path => the specific content
            int posBegin = params[0].indexOf("(");
            int posEnd = params[0].indexOf(")");
            if ((posBegin >= 0) && (posEnd >= 0)) {
                int part = Integer.valueOf(params[0].substring(posBegin + 1, posEnd));
                if (multipartArray != null) {
                    try {
                        sdpContent = multipartArray[part].trim();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new ExecutionException("Error in content:Sdp : " + e.getMessage());
                    }
                } else {
                    sdpContent = content;
                }
            }
            sdpContent = sdpContent.substring(sdpContent.indexOf("\r\n\r\n") + 1, sdpContent.length()).trim();
            if (sdpContent == null || sdpContent.length() <= 0) {
                return;
            }
            PluggableParameterOperatorSetFromSDP.addSDPParameter(var, params, sdpContent, 2, path);

        }
    }

    /** Get the parts content of this message */
    private static String[] contentSDPPart2(String content, String contentBoundary) throws Exception {
        String[] multipartArray = null;
        String[] multipartFinalArray;
        if (contentBoundary != null) {
            multipartArray = Utils.splitNoRegex(content, contentBoundary);
            multipartFinalArray = new String[multipartArray.length];
            int j = 0;
            for (int i = 0; i < multipartArray.length; i++) {
                if (multipartArray[i].length() > 0) {
                    multipartFinalArray[j] = multipartArray[i];
                    j++;
                }
            }
            return multipartFinalArray;
        }
        multipartFinalArray = new String[1];
        multipartFinalArray[0] = content;
        return multipartFinalArray;
    }

    public String[] contentSDPPart(String protocol, String content, String contentBoundary) throws Exception {
        // extract the Content-Type header and needed informations (firstId / mimeSeparator)
        String localMimeSeparator = null;

        if (null != contentBoundary) {
            localMimeSeparator = "--" + contentBoundary;
        } else if (null == contentBoundary && protocol.equals("MGCP")) {
            localMimeSeparator = "\r\n\r\n";
        } else {

            return new String[]{content};
        }

        // if the content starts with a separator, we remove it to avoid having
        // an empty cell in the output array of split method
        if (!protocol.equals("MGCP")) {
            if (content.startsWith(localMimeSeparator)) {
                content = content.substring(localMimeSeparator.length());
            }
            if (content.endsWith("\r\n" + localMimeSeparator + "--")) {
                content = content.substring(0, content.length() - localMimeSeparator.length() - 4);
            }
            if (content.endsWith("\n" + localMimeSeparator + "--")) {
                content = content.substring(0, content.length() - localMimeSeparator.length() - 3);
            }
        }

        // populate the different hashmaps
        if (protocol.equals("MGCP")){
            String[] parts = Utils.splitNoRegex(content, localMimeSeparator);
            return parts;
        }

        String[] parts = Utils.splitNoRegex(content, "\r\n" + localMimeSeparator + "\r\n");
//        HashMap<String, String> contents = new HashMap();
//        HashMap<String, String> types = new HashMap();
//        HashMap<String, String> encodings = new HashMap();
//
//        for(String string:parts)
//        {
//            // extract id
//            String idHeader = string.substring(string.toLowerCase().indexOf("content-id"));
//            idHeader = idHeader.split("[\\r]?\\n[\\r]?\\n", 2)[0];
//            idHeader = idHeader.split("[\\r]?\\n\\p{Alnum}", 2)[0];
//
//            String id = idHeader.split(":", 2)[1].replace("<", "").replace(">", "").trim();
//            contents.put(id, string.split("[\\r]?\\n[\\r]?\\n", 3)[1]);
//
//            // extract types
//            String typeHeader = string.substring(string.toLowerCase().indexOf("content-type"));
//            typeHeader = typeHeader.split("[\\r]?\\n[\\r]?\\n", 2)[0];
//            typeHeader = typeHeader.split("[\\r]?\\n\\p{Alnum}", 2)[0];
//            String type = typeHeader.split(":", 2)[1].trim();
//            types.put(id, type);
//
//            // extract encoding
//            String encodingHeader = string.substring(string.toLowerCase().indexOf("content-transfer-encoding"));
//            encodingHeader = encodingHeader.split("[\\r]?\\n[\\r]?\\n", 2)[0];
//            encodingHeader = encodingHeader.split("[\\r]?\\n\\p{Alnum}", 2)[0];
//            String encoding = encodingHeader.split(":", 2)[1].trim();
//            encodings.put(id, encoding);
//        }

        // now, get the XML and compute it
        return parts;
    }
}
