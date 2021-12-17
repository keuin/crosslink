package com.keuin.crosslink.messaging.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

class RuleListsTest {

    @Test
    void fromJson() throws JsonProcessingException {
        var json = new String(Base64.getDecoder()
                .decode("WwogICAgLy8gYWxsIHJ1bGVzIGFyZSBwcm9jZXNzZWQgc2VxdWVudGlhbGx5CiAgICAvLyBhIG1lc3NhZ2UgbWF5IG1hdGNoIG11bHRpcGxlIHJ1bGVzIGFuZCB0aHVzIG1heSBiZSBkdXBsaWNhdGUgaW4geW91ciBjYXNlCiAgICAvLyBpZiB0aGUgbWVzc2FnZSBpcyBkcm9wcGVkIGluIGFuIGFjdGlvbiBpbiBvbmUgcnVsZSwKICAgIC8vICh0aGUgYWN0aW9uIHR5cGUgaXMganVzdCAiZHJvcCIgYW5kIGl0IGRvZXMgbm90IGhhdmUgYW55IGFyZ3VtZW50KQogICAgLy8gYWxsIHN1YnNlcXVlbnQgcnVsZXMgd2lsbCBOT1Qgc2VlIHRoaXMgbWVzc2FnZQogICAgewogICAgICAgIC8vIGluYm91bmQgY2hhdCBtZXNzYWdlcyAocmVtb3RlIC0+IGFsbCBzZXJ2ZXJzKQogICAgICAgICJvYmplY3QiOiAiY2hhdF9tZXNzYWdlIiwgLy8gbWF0Y2ggY2hhdCBtZXNzYWdlcwogICAgICAgICJmcm9tIjogInJlbW90ZTouKiIsICAgICAgLy8gcmVnZXhwIG1hdGNoaW5nIHNvdXJjZSwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIC8vIG9ubHkgbWVzc2FnZXMgd2l0aCBtYXRjaGVkIHNvdXJjZSB3aWxsIGJlCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAvLyBwcm9jZXNzZWQgYnkgdGhpcyBydWxlLCBvdGhlcndpc2UgdGhpcyBydWxlIGlzIHNraXBwZWQKICAgICAgICAiYWN0aW9ucyI6IFt7ICAgICAgICAgICAgIC8vIGFjdGlvbnMgcnVuIHNlcXVlbnRpYWxseQogICAgICAgICAgICAidHlwZSI6ICJyb3V0ZSIsICAgICAgLy8gcm91dGUgdGhpcyBtZXNzYWdlIHRvIG1hdGNoZWQgZGVzdGluYXRpb25zCiAgICAgICAgICAgICJ0byI6ICJzZXJ2ZXI6LioiICAgICAvLyByZWdleHAgbWF0Y2hpbmcgZGVzdGluYXRpb24gIAogICAgICAgIH1dCiAgICB9LAogICAgewogICAgICAgIC8vIG91dGJvdW5kIG1lc3NhZ2VzIChzdGFydGluZyB3aXRoICcjJywgc2VydmVyIC0+IGFsbCByZW1vdGVzKQogICAgICAgICJvYmplY3QiOiAiY2hhdF9tZXNzYWdlIiwKICAgICAgICAiZnJvbSI6ICJzZXJ2ZXI6LioiLAogICAgICAgICJhY3Rpb25zIjogW3sKICAgICAgICAgICAgInR5cGUiOiAiZmlsdGVyIiwgICAgIC8vIGZpbHRlciB0aGUgbWVzc2FnZSB1c2luZyBnaXZlbiByZWdleHAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIC8vIGlmIHRoZSBtZXNzYWdlIGRvZXMgbm90IG1hdGNoIGdpdmVuIHBhdHRlcm4sCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAvLyBpdCB3b24ndCBiZSBwYXNzZWQgaW50byBzdWJzZXF1ZW50IGFjdGlvbnMKICAgICAgICAgICAgInBhdHRlcm4iOiAiIy4rIiAgICAgIC8vIG1hdGNoIGFsbCBtZXNzYWdlcyBzdGFydHMgd2l0aCBjaGFyICcjJwogICAgICAgIH0sIHsKICAgICAgICAgICAgInR5cGUiOiAicmVwbGFjZSIsICAgIC8vIHJlcGxhY2UgdGhlIG1lc3NhZ2UsIHJlbW92aW5nIGhlYWRpbmcgJyMnCiAgICAgICAgICAgICJmcm9tIjogIl4jXFwoLipcXCkiLCAvLyBjYXB0dXJlIGFsbCBjaGFycyBhZnRlciB0aGUgaGVhZGluZyAnIycKICAgICAgICAgICAgInRvIjogIiQxIiAgICAgICAgICAgIC8vIGFuZCBtYWtlIHRoZW0gYXMgdGhlIG91dHB1dAogICAgICAgIH0sIHsKICAgICAgICAgICAgInR5cGUiOiAicm91dGUiLCAgICAgIC8vIHNlbmQgdGhlIG1lc3NhZ2UgdG8gYWxsIHJlbW90ZXMKICAgICAgICAgICAgInRvIjogInJlbW90ZTouKiIKICAgICAgICB9XQogICAgfSwKICAgIHsKICAgICAgICAvLyBjcm9zcy1zZXJ2ZXIgbWVzc2FnZXMgKHNlcnZlciAtPiBhbGwgb3RoZXIgc2VydmVycykKICAgICAgICAib2JqZWN0IjogImNoYXRfbWVzc2FnZSIsCiAgICAgICAgImZvcm0iOiAic2VydmVyOi4qIiwKICAgICAgICAiYWN0aW9ucyI6IFt7CiAgICAgICAgICAgICJ0eXBlIjogInJvdXRlIiwKICAgICAgICAgICAgInRvIjogInNlcnZlcjouKiIsCiAgICAgICAgICAgICJiYWNrZmxvdyI6IGZhbHNlICAvLyBkbyBub3QgcmVwZWF0IHRvIHNlbmRlciwgdHJ1ZSBieSBkZWZhdWx0CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAvLyBzaW5jZSB0aGUgZGVzdGluYXRpb24gcGF0dGVybiB3aWxsIG1hdGNoIHRoZSBzb3VyY2UsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAvLyB3ZSBoYXZlIHRvIHNldCBiYWNrZmxvdyB0byBmYWxzZSB0byBwcmV2ZW50CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAvLyBwbGF5ZXJzIGZyb20gc2VlaW5nIGR1cGxpY2F0ZSBtZXNzYWdlcwogICAgICAgIH1dCiAgICB9Cl0="),
                StandardCharsets.UTF_8);
        var mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        var t = mapper.readTree(json);
        for (var rule : t) {
            var object = rule.get("object");
            var from = rule.get("from");
            var actions = rule.get("actions");
            System.out.printf("%s | %s | %s%n", object, from, actions);
        }
    }

    @Test
    void testReadObjectType() {

    }

    @Test
    void testReadReIdFilter() {

    }
}