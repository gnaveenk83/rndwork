package com.etech.rules.processor.util;

import org.w3c.dom.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlUtil {

    private static final ThreadLocal<DocumentBuilder> threadLocalBuilder = ThreadLocal.withInitial(() -> {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DocumentBuilder", e);
        }
    });

    public static Document parse(String xml) {
        try {
            return threadLocalBuilder.get().parse(new ByteArrayInputStream(xml.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML", e);
        }
    }

    public static Node getNodeByXPath(Document doc, String xpathExpr, Map<String, String> namespaces) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new SimpleNamespaceContext(namespaces));
            return (Node) xpath.evaluate(xpathExpr, doc, XPathConstants.NODE);
        } catch (Exception e) {
            throw new RuntimeException("XPath evaluation failed", e);
        }
    }

    public static void replaceTextByXPath(Document doc, String xpathExpr, String newText, Map<String, String> namespaces) {
        Node node = getNodeByXPath(doc, xpathExpr, namespaces);
        if (node != null) {
            node.setTextContent(newText);
        }
    }

    public static void deleteNodeByXPath(Document doc, String xpathExpr, Map<String, String> namespaces) {
        Node node = getNodeByXPath(doc, xpathExpr, namespaces);
        if (node != null && node.getParentNode() != null) {
            node.getParentNode().removeChild(node);
        }
    }

    public static void setAttribute(Element element, String name, String value) {
        if (element != null && name != null) {
            element.setAttribute(name, value);
        }
    }

    public static String toString(Node node) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            throw new RuntimeException("Failed to transform node to string", e);
        }
    }

    private static class SimpleNamespaceContext implements NamespaceContext {
        private final Map<String, String> prefixMap;

        public SimpleNamespaceContext(Map<String, String> prefixMap) {
            this.prefixMap = prefixMap;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return prefixMap.getOrDefault(prefix, "");
        }

        @Override
        public String getPrefix(String namespaceURI) {
            for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            return prefixMap.keySet().iterator();
        }
    }
}
