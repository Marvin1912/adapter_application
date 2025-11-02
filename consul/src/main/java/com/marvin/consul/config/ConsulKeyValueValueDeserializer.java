package com.marvin.consul.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Base64;

public class ConsulKeyValueValueDeserializer extends JsonDeserializer<String> {

  @Override
  public String deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {

    JsonNode treeNode = parser.getCodec().readTree(parser);
    if (!treeNode.isTextual()) {
      return null;
    }

    String value = treeNode.asText();
    return new String(Base64.getDecoder().decode(value));
  }
}
