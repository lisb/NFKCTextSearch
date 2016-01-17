package com.lisb.nfkctextsearch;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NFKCTextSearch {

    private final String normalizedPattern;
    private final boolean ignoreCase;

    public NFKCTextSearch(String pattern, boolean ignoreCase) {
        this.ignoreCase = ignoreCase;

        if (ignoreCase) {
            pattern = pattern.toUpperCase(Locale.ENGLISH);
        }
        this.normalizedPattern = Normalizer.normalize(pattern, Normalizer.Form.NFKC);
    }

    public List<HitSpan> search(String text) {
        final StringBuilder builder = new StringBuilder();
        final List<Integer> startPositions = new ArrayList<Integer>(text.length());
        if (ignoreCase) {
            text = text.toUpperCase(Locale.ENGLISH);
        }
        normalize(text, builder, startPositions);

        int position = 0;
        int index;
        final List<HitSpan> result = new ArrayList<HitSpan>();
        while ((index = builder.indexOf(normalizedPattern, position)) != -1) {
            position = index + normalizedPattern.length();
            final Integer start = startPositions.get(index);
            Integer end = null;
            for (int i = position; i < startPositions.size(); i++) {
                end = startPositions.get(i);
                if (start.equals(end)) {
                    end = null;
                } else {
                    break;
                }
            }

            if (end == null) {
                end = text.length();
            }
            result.add(new HitSpan(start, end));
        }

        return result;
    }

    /**
     * @param startPositions start position of characters before normalization.
     */
    static void normalize(String text, StringBuilder dest, List<Integer> startPositions) {
        final int length = text.length();

        // Buffer to store normalized text.
        // If the next character don't affect current buffered characters, it concatenate to dest.
        final StringBuilder buffer = new StringBuilder();
        // start position of first buffering character before normalization
        int bufferStartPosition = 0;

        for (int i = 0; i < length; i++) {
            final int bufferLength = buffer.length();
            final String nextBufferText = buffer.toString() + text.charAt(i);
            if (!Normalizer.isNormalized(nextBufferText, Normalizer.Form.NFKC)) {
                final String normalizedText = Normalizer.normalize(nextBufferText, Normalizer.Form.NFKC);
                if (normalizedText.startsWith(buffer.toString())) {
                    // Because buffering character is not changed, buffer concatenate to dest.
                    dest.append(buffer.substring(0, bufferLength));
                    buffer.delete(0, bufferLength);
                    buffer.append(normalizedText.substring(bufferLength, normalizedText.length()));

                    for (int j = 0; j < bufferLength; j++) {
                        startPositions.add(bufferStartPosition);
                    }
                    bufferStartPosition = i;
                } else {
                    buffer.delete(0, bufferLength).append(normalizedText);
                }
            } else {
                // Because the next character is normalized, buffer concatenate to dest.
                dest.append(buffer);
                buffer.delete(0, bufferLength);
                buffer.append(text.charAt(i));

                for (int j = 0; j < bufferLength; j++) {
                    startPositions.add(bufferStartPosition);
                }
                bufferStartPosition = i;
            }
        }

        dest.append(buffer);
        for (int i = 0; i < buffer.length(); i++) {
            startPositions.add(bufferStartPosition);
        }
    }

    public static class HitSpan {
        public final int start;
        public final int end;

        public HitSpan(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HitSpan) {
                final HitSpan span = (HitSpan) obj;
                return span.start == start && span.end == end;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return start + end * 13;
        }

        @Override
        public String toString() {
            return "HitSpan{start:" + start + ",end:" + end + "}";
        }
    }

}
