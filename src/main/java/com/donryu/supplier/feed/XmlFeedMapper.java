package com.donryu.supplier.feed;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class XmlFeedMapper implements FeedMapper {

    private static final String PRODUCT = "product";
    private static final String SKU = "sku";
    private static final String NAME = "productName";
    private static final String QUANTITY = "stock";
    private static final String PRICE = "cost";

    @Override
    public FeedFormat format() {
        return FeedFormat.XML;
    }

    @Override
    public FeedMapResult map(InputStream input) {
        Document document = parse(input);
        NodeList products = document.getElementsByTagName(PRODUCT);

        List<ProductRecord> records = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < products.getLength(); i++) {
            Element product = (Element) products.item(i);
            try {
                records.add(ProductRecordParser.parse(
                        text(product, SKU), text(product, NAME), text(product, QUANTITY), text(product, PRICE)));
            } catch (IllegalArgumentException e) {
                errors.add("Product " + (i + 1) + ": " + e.getMessage());
            }
        }
        return new FeedMapResult(records, errors);
    }

    private Document parse(InputStream input) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new SilentErrorHandler());
            return builder.parse(input);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new FeedParseException("Could not read XML feed: " + e.getMessage(), e);
        }
    }

    private String text(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() == 0 ? null : nodes.item(0).getTextContent();
    }

    private static class SilentErrorHandler implements ErrorHandler {

        @Override
        public void warning(SAXParseException e) {
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }
}
