package com.lisb.nfkctextsearch;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NFKCTextSearchTest {

    @Test
    public void testSearch() throws Exception {
        Assert.assertEquals(Collections.singletonList(new NFKCTextSearch.HitSpan(1, 4)),
                new NFKCTextSearch("234", false).search("12345"));

        Assert.assertEquals(Arrays.asList(new NFKCTextSearch.HitSpan(0, 1), new NFKCTextSearch.HitSpan(1, 2)),
                new NFKCTextSearch("A", false).search("AＡaａ"));
        Assert.assertEquals(Arrays.asList(new NFKCTextSearch.HitSpan(0, 1), new NFKCTextSearch.HitSpan(1, 2),
                new NFKCTextSearch.HitSpan(2, 3), new NFKCTextSearch.HitSpan(3, 4)),
                new NFKCTextSearch("A", true).search("AＡaａ"));
        Assert.assertEquals(Arrays.asList(new NFKCTextSearch.HitSpan(0, 2), new NFKCTextSearch.HitSpan(2, 4)),
                new NFKCTextSearch("AA", true).search("AＡaａ"));

        Assert.assertEquals(Arrays.asList(new NFKCTextSearch.HitSpan(1, 2), new NFKCTextSearch.HitSpan(3, 4)),
                new NFKCTextSearch("キロ", false).search("1㌔2㌔"));
        Assert.assertEquals(Arrays.asList(new NFKCTextSearch.HitSpan(1, 2), new NFKCTextSearch.HitSpan(3, 4)),
                new NFKCTextSearch("キ", false).search("1㌔2㌔"));
        Assert.assertEquals(Collections.singletonList(new NFKCTextSearch.HitSpan(0, 2)),
                new NFKCTextSearch("ロキ", false).search("㌔㌔"));

        Assert.assertEquals(Arrays.asList(new NFKCTextSearch.HitSpan(0, 2), new NFKCTextSearch.HitSpan(4, 6)),
                new NFKCTextSearch("𠮷", true).search("𠮷野家𠮷野家"));

        Assert.assertEquals(Collections.emptyList(), new NFKCTextSearch("", true).search("ABCDE"));
    }

    @Test
    public void testNormalize() throws Exception {
        final List<Integer> startPositions = new ArrayList<Integer>();
        final StringBuilder builder = new StringBuilder();

        // test empty string
        NFKCTextSearch.normalize("", builder, startPositions);
        Assert.assertEquals("", builder.toString());
        Assert.assertEquals(Collections.emptyList(), startPositions);

        builder.delete(0, builder.length());
        startPositions.clear();

        // ṩ(Source) ṩ(NFD) ṩ(NFC)
        NFKCTextSearch.normalize("\u1e69\u0073\u0323\u0307\u1e69", builder, startPositions);
        Assert.assertEquals("ṩṩṩ", builder.toString());
        Assert.assertEquals(Arrays.asList(0, 1, 4), startPositions);

        builder.delete(0, builder.length());
        startPositions.clear();

        // ḍ̇(Source) ḍ̇(NFD)  ḍ̇(NFC)
        NFKCTextSearch.normalize("\u1e0b\u0323\u0064\u0323\u0307\u1e0d\u0307", builder, startPositions);
        Assert.assertEquals("ḍ̇ḍ̇ḍ̇", builder.toString());
        Assert.assertEquals(Arrays.asList(0, 0, 2, 4, 5, 6), startPositions);

        builder.delete(0, builder.length());
        startPositions.clear();

        // 1㌢㍍3㌔ṩ(Source) 1センチメートル3㌔ṩ(NFKC)
        NFKCTextSearch.normalize("1㌢㍍3㌔ṩ", builder, startPositions);
        Assert.assertEquals("1センチメートル3キロṩ", builder.toString());
        Assert.assertEquals(Arrays.asList(0, 1, 1, 1, 2, 2, 2, 2, 3, 4, 4, 5), startPositions);

        builder.delete(0, builder.length());
        startPositions.clear();

        // Äffin(NFD) Äffin(NFC)
        NFKCTextSearch.normalize("A\u0308ffin", builder, startPositions);
        Assert.assertEquals("Äffin", builder.toString());
        Assert.assertEquals(Arrays.asList(0, 2, 3, 4, 5), startPositions);

        builder.delete(0, builder.length());
        startPositions.clear();

        NFKCTextSearch.normalize("\uD867\uDE3Dのひらき", builder, startPositions);
        Assert.assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5), startPositions);

        builder.delete(0, builder.length());
        startPositions.clear();

        NFKCTextSearch.normalize("𠮷野家𠮷野家", builder, startPositions);
        Assert.assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7), startPositions);
        Assert.assertEquals("𠮷野家𠮷野家", builder.toString());
    }

}
