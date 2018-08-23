/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.itl.ssd.wipp.backend.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Convert file names using patterns similar to the ones used by MIST.
 *
 * @author Antoine Vandecreme
 */
public class PatternFilenameConverter extends FilenameConverter {

    private final String sourcePattern;
    private final String destPattern;
    private final Pattern sourceRegexPattern;
    private final String destTemplate;
    private final Map<Replacement, Replacement> replacements;
    private static final Pattern PATTERN = Pattern.compile("\\{[a-z]+\\}");

    public PatternFilenameConverter(String sourcePattern, String destPattern) {
        this.sourcePattern = sourcePattern;
        this.destPattern = destPattern;
        this.replacements = new HashMap<>();

        List<Replacement> sourceReplacements = getReplacements(sourcePattern);
        StringBuilder sourcePatternBuilder = new StringBuilder(sourcePattern);
        int offset = 0;
        for (Replacement sourceRepl : sourceReplacements) {
            String regexp = "[0-9]{" + sourceRepl.length + "}";
            sourcePatternBuilder.replace(
                    sourceRepl.start + offset,
                    sourceRepl.end + offset + 2,
                    regexp);
            offset += regexp.length() - sourceRepl.end + sourceRepl.start;
        }
        this.sourceRegexPattern = Pattern.compile(sourcePatternBuilder.toString());

        List<Replacement> destReplacements = getReplacements(destPattern);
        StringBuilder destTemplateBuilder = new StringBuilder(destPattern);
        for (Replacement destRepl : destReplacements) {
            destTemplateBuilder.replace(
                    destRepl.start,
                    destRepl.end + 2,
                    StringUtils.repeat(destRepl.letter + "", destRepl.length));
        }
        this.destTemplate = destTemplateBuilder.toString();

        for (Replacement sourceRepl : sourceReplacements) {
            Optional<Replacement> destReplOpt = destReplacements.stream()
                    .filter(r -> r.letter == sourceRepl.letter).findAny();
            if (!destReplOpt.isPresent()) {
                throw new IllegalArgumentException("Replacement pattern "
                        + sourceRepl + " not present in destination.");
            }
            Replacement destRepl = destReplOpt.get();
            if (destRepl.length != sourceRepl.length) {
                throw new IllegalArgumentException("Replacement patterns "
                        + sourceRepl + " and " + destRepl
                        + " do not have the same length.");
            }
            replacements.put(sourceRepl, destRepl);
            destReplacements.remove(destRepl);
        }

        for (Replacement destRepl : destReplacements) {
            throw new IllegalArgumentException("Replacement pattern "
                    + destRepl + " not present in source.");
        }
    }

    private static List<Replacement> getReplacements(String pattern) {
        int offset = 0;
        List<Replacement> result = new ArrayList<>();
        Matcher destMatcher = PATTERN.matcher(pattern);
        while (destMatcher.find()) {
            int start = destMatcher.start() - offset;
            int end = destMatcher.end() - offset - 2;
            char letter = pattern.charAt(destMatcher.start() + 1);

            if (result.stream().filter(r -> r.letter == letter)
                    .findAny().isPresent()) {
                throw new IllegalArgumentException("The pattern letter '"
                        + letter + "' is used multiple times.");
            }

            result.add(new Replacement(letter, start, end));
            offset += 2;
        }
        return result;
    }

    private static class Replacement {

        private final char letter;
        private final int start;
        private final int end;
        private final int length;

        private Replacement(char letter, int start, int end) {
            this.letter = letter;
            this.start = start;
            this.end = end;
            this.length = end - start;
        }

        @Override
        public String toString() {
            return '{' + StringUtils.repeat(letter + "", length) + '}';
        }
    }

    @Override
    public boolean canConvert(String fileName) {
        return sourceRegexPattern.matcher(fileName).matches();
    }

    /**
     * Convert the given fileName by matching the regex and replacing each group
     * by it corresponding replacer
     *
     * @param fileName
     * @return the converted file name
     */
    @Override
    public String convert(String fileName) {
        if (!canConvert(fileName)) {
            throw new IllegalArgumentException("The file " + fileName
                    + " does not match the provided pattern.");
        }

        StringBuilder sb = new StringBuilder(destTemplate);
        for (Entry<Replacement, Replacement> entry : replacements.entrySet()) {
            Replacement sourceRepl = entry.getKey();
            Replacement destRepl = entry.getValue();
            sb.replace(
                    destRepl.start,
                    destRepl.end,
                    fileName.substring(sourceRepl.start, sourceRepl.end));
        }
        return sb.toString();
    }

    /**
     * Returns a PatternFilenameConverter with sourcePattern and destPattern
     * interchanged.
     *
     * @return the opposite PatternFilenameConverter
     */
    @Override
    public PatternFilenameConverter createOpposite() {
        return new PatternFilenameConverter(destPattern, sourcePattern);
    }

}
