package com.github.seregamorph.testdistribution.maven;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Sergey Chernov
 */
final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setDefaultPrettyPrinter(createPrettyPrinter());

    private static PrettyPrinter createPrettyPrinter() {
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("  ", DefaultIndenter.SYS_LF);
        prettyPrinter.indentArraysWith(indenter);
        return prettyPrinter;
    }

    static void writeEntity(File file, TestDistributionEntity entity) {
        try {
            MAPPER.writeValue(file, entity);
        } catch (IOException e) {
            throw new UncheckedIOException("Error while writing " + file, e);
        }
    }

    static TestDistributionEntity readEntity(File file) {
        try {
            return MAPPER.readValue(file, TestDistributionEntity.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Error while reading " + file, e);
        }
    }

    private JsonUtils() {
    }
}
