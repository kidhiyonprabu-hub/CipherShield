package com.ciphershield.service;

import com.ciphershield.model.ScanResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * PrivacyGuard scanner: detects sensitive data patterns (emails, phone numbers, student IDs)
 * using Java regular expressions.
 *
 * CO4: String manipulation and regular expressions.
 * Security Note: Local pattern detection only. Never transmits data externally or persists sensitive findings.
 */
public class PrivacyScanner {

    // RFC 5322 simplified email regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b"
    );

    // North American & International phone number formats
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\b(?:\\+?\\d{1,3}[- .]?)?\\(?\\d{3}\\)?[- .]?\\d{3}[- .]?\\d{4}\\b"
    );

    // Common Student / Campus ID patterns, e.g. STU-123456, ID: 12345678, CS2024-9988
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile(
            "\\b(?:STU[-_]?\\d{5,8}|ID[:#]?\\s*\\d{6,10}|[A-Z]{2,4}\\d{4}[-_]?\\d{3,6})\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Scans arbitrary text and returns detected matches.
     */
    public List<ScanResult> scanText(String text) {
        List<ScanResult> results = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return results;
        }

        String[] lines = text.split("\\r?\\n");
        int globalOffset = 0;

        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
            String line = lines[lineIdx];
            int lineNum = lineIdx + 1;

            // 1. Email check
            Matcher emailMatcher = EMAIL_PATTERN.matcher(line);
            while (emailMatcher.find()) {
                results.add(new ScanResult(
                        ScanResult.Category.EMAIL,
                        emailMatcher.group(),
                        lineNum,
                        globalOffset + emailMatcher.start()
                ));
            }

            // 2. Phone check
            Matcher phoneMatcher = PHONE_PATTERN.matcher(line);
            while (phoneMatcher.find()) {
                results.add(new ScanResult(
                        ScanResult.Category.PHONE,
                        phoneMatcher.group(),
                        lineNum,
                        globalOffset + phoneMatcher.start()
                ));
            }

            // 3. Student ID check
            Matcher idMatcher = STUDENT_ID_PATTERN.matcher(line);
            while (idMatcher.find()) {
                results.add(new ScanResult(
                        ScanResult.Category.STUDENT_ID,
                        idMatcher.group(),
                        lineNum,
                        globalOffset + idMatcher.start()
                ));
            }

            globalOffset += line.length() + 1;
        }

        return results;
    }

    public List<ScanResult> scanFile(File file) throws IOException {
        return scanText(readFileContent(file));
    }

    public String readFileContent(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IOException("Selected path does not exist or is not a regular file.");
        }
        if (!file.canRead()) {
            throw new IOException("Selected file cannot be read. Check its permissions.");
        }
        if (file.length() == 0) {
            throw new IOException("Selected file is empty.");
        }

        String name = file.getName().toLowerCase(Locale.ROOT);
        String content;
        if (name.endsWith(".txt")) {
            StringBuilder text = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(java.nio.file.Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
                char[] buffer = new char[8192];
                int count;
                while ((count = reader.read(buffer)) != -1) {
                    text.append(buffer, 0, count);
                }
            }
            content = text.toString();
        } else if (name.endsWith(".docx")) {
            content = readDocxContent(file);
        } else {
            throw new IOException("Unsupported file type. PrivacyGuard supports .txt and .docx files.");
        }

        if (content.trim().isEmpty()) {
            throw new IOException("No readable text content was found in the selected file.");
        }
        return content;
    }

    private String readDocxContent(File file) throws IOException {
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(file);
        } catch (IOException e) {
            throw new IOException("Could not open DOCX document. The file may be malformed or corrupted.", e);
        }
        try (ZipFile document = zipFile) {
            ZipEntry documentXml = document.getEntry("word/document.xml");
            if (documentXml == null || documentXml.isDirectory()) {
                throw new IOException("Invalid DOCX document: word/document.xml is missing.");
            }

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            final StringBuilder text = new StringBuilder();
            DefaultHandler handler = new DefaultHandler() {
                private boolean insideText;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    if ("t".equals(localName) || qName.endsWith(":t")) {
                        insideText = true;
                    }
                }

                @Override
                public void characters(char[] chars, int start, int length) {
                    if (insideText) {
                        text.append(chars, start, length);
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) {
                    if ("t".equals(localName) || qName.endsWith(":t")) {
                        insideText = false;
                    } else if ("p".equals(localName) || qName.endsWith(":p")) {
                        text.append('\n');
                    }
                }
            };

            try (InputStream stream = document.getInputStream(documentXml)) {
                factory.newSAXParser().parse(stream, handler);
            } catch (Exception e) {
                throw new IOException("Could not read DOCX document. The file may be malformed or corrupted.", e);
            }
            return text.toString();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Could not open DOCX document. The file may be malformed or corrupted.", e);
        }
    }
}
