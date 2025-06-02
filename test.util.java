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


    public class XPathSteps {

    private final ScenarioContext context;

    public XPathSteps(ScenarioContext context) {
        this.context = context;
    }

    @When("I replace the text at XPath {string} with {string}")
    public void replaceTextAtXPath(String xpath, String newText) {
        Document doc = context.get("doc", Document.class);
        Map<String, String> namespaces = context.get("namespaces", Map.class);
        XmlUtil.replaceTextByXPath(doc, xpath, newText, namespaces);
        context.put("doc", doc);
    }

    @When("I delete the element at XPath {string}")
    public void deleteElementAtXPath(String xpath) {
        Document doc = context.get("doc", Document.class);
        Map<String, String> namespaces = context.get("namespaces", Map.class);
        XmlUtil.deleteNodeByXPath(doc, xpath, namespaces);
        context.put("doc", doc);
    }

    @When("I update the attribute {string} of XPath {string} to {string}")
    public void updateAttributeAtXPath(String attrName, String xpath, String value) {
        Document doc = context.get("doc", Document.class);
        Map<String, String> namespaces = context.get("namespaces", Map.class);
        Element element = (Element) XmlUtil.getNodeByXPath(doc, xpath, namespaces);
        XmlUtil.setAttribute(element, attrName, value);
        context.put("doc", doc);
    }

    @When("I delete the attribute {string} from XPath {string}")
    public void deleteAttributeFromXPath(String attrName, String xpath) {
        Document doc = context.get("doc", Document.class);
        Map<String, String> namespaces = context.get("namespaces", Map.class);
        Element element = (Element) XmlUtil.getNodeByXPath(doc, xpath, namespaces);
        if (element != null) element.removeAttribute(attrName);
        context.put("doc", doc);
    }

    @Then("the resulting request should contain XPath {string} with text {string}")
    public void assertTextAtXPath(String xpath, String expectedText) {
        Document doc = context.get("doc", Document.class);
        Map<String, String> namespaces = context.get("namespaces", Map.class);
        Node node = XmlUtil.getNodeByXPath(doc, xpath, namespaces);
        Assertions.assertNotNull(node, "Expected node not found");
        Assertions.assertEquals(expectedText, node.getTextContent().trim());
    }

    @Then("the resulting request should not contain XPath {string}")
    public void assertNodeNotPresent(String xpath) {
        Document doc = context.get("doc", Document.class);
        Map<String, String> namespaces = context.get("namespaces", Map.class);
        Node node = XmlUtil.getNodeByXPath(doc, xpath, namespaces);
        Assertions.assertNull(node, "Node should not exist but was found");
    }

    @Then("the attribute {string} of XPath {string} should be {string}")
    public void assertAttributeAtXPath(String attr, String xpath, String expected) {
        Document doc = context.get("doc", Document.class);
        Map<String, String> namespaces = context.get("namespaces", Map.class);
        Element el = (Element) XmlUtil.getNodeByXPath(doc, xpath, namespaces);
        Assertions.assertNotNull(el);
        Assertions.assertEquals(expected, el.getAttribute(attr));
    }

    @Then("the attribute {string} of XPath {string} should not exist")
    public void assertAttributeNotPresent(String attr, String xpath) {
        Document doc = context.get("doc", Document.class);
        Map<String, String> namespaces = context.get("namespaces", Map.class);
        Element el = (Element) XmlUtil.getNodeByXPath(doc, xpath, namespaces);
        Assertions.assertNotNull(el);
        Assertions.assertFalse(el.hasAttribute(attr), "Attribute " + attr + " should not exist");
    }
}
}

Feature: Modify Purchase Order XML using XPath

  Background:
    Given a sample purchase order request is loaded

  Scenario: Replace City text content using XPath
    When I replace the text at XPath "//po:City" with "London"
    Then the resulting request should contain XPath "//po:City" with text "London"

  Scenario: Delete Address element using XPath
    When I delete the element at XPath "//po:Address"
    Then the resulting request should not contain XPath "//po:Address"

  Scenario: Update postcode attribute of po:City using XPath
    When I update the attribute "postcode" of XPath "//po:City" to "SW1A 1AA"
    Then the attribute "postcode" of XPath "//po:City" should be "SW1A 1AA"

  Scenario: Delete postcode attribute of po:City using XPath
    When I delete the attribute "postcode" from XPath "//po:City"
    Then the attribute "postcode" of XPath "//po:City" should not exist





    public static Node getNodeByXPath(Document doc, String xpathExpr, Map<String, String> namespaces) {
    try {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                return namespaces.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
            }

            public String getPrefix(String namespaceURI) {
                return namespaces.entrySet().stream()
                        .filter(e -> e.getValue().equals(namespaceURI))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);
            }

            public Iterator<String> getPrefixes(String namespaceURI) {
                return namespaces.keySet().iterator();
            }
        });

        // NOTE: Wrap XPath in () to use global index
        XPathExpression expr = xpath.compile(xpathExpr);
        return (Node) expr.evaluate(doc, XPathConstants.NODE);
    } catch (Exception e) {
        throw new RuntimeException("XPath evaluation failed: " + xpathExpr, e);
    }
}
