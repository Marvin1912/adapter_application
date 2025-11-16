package com.marvin.vocabulary.dictionaryapi;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class HtmlCleaner {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern HTML_ENTITY_PATTERN = Pattern.compile("&[a-zA-Z]+;");

    public String cleanHtml(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Remove HTML tags
        String cleaned = HTML_TAG_PATTERN.matcher(text).replaceAll("");

        // Replace common HTML entities with their character equivalents
        cleaned = cleaned.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&nbsp;", " ")
                .replace("&mdash;", "\u2014")  // em dash
                .replace("&ndash;", "\u2013")  // en dash
                .replace("&ldquo;", "\u201C")  // left double quote
                .replace("&rdquo;", "\u201D")  // right double quote
                .replace("&lsquo;", "\u2018")  // left single quote
                .replace("&rsquo;", "\u2019"); // right single quote

        // Clean up any remaining HTML entities
        cleaned = HTML_ENTITY_PATTERN.matcher(cleaned).replaceAll("");

        // Normalize whitespace
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned;
    }
}
