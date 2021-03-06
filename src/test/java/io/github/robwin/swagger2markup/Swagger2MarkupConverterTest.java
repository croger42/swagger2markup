/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.swagger2markup;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.extension.Swagger2MarkupExtensionRegistry;
import io.github.robwin.swagger2markup.extension.repository.DynamicDefinitionsContentExtension;
import io.github.robwin.swagger2markup.extension.repository.DynamicOperationsContentExtension;
import io.swagger.models.Swagger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.assertj.core.api.BDDAssertions.assertThat;

public class Swagger2MarkupConverterTest {

    @Test
    public void testSwagger2AsciiDocConversionFromString() throws IOException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/json/swagger.json"));
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConverter.from(swaggerJsonString).build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocConversionAsString() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        String asciiDocAsString = Swagger2MarkupConverter.from(file.toURI()).build()
                .asString();
        //Then
        assertThat(asciiDocAsString).isNotEmpty();
    }


    @Test
    public void testSwagger2AsciiDocConversion() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConverter.from(file.toURI()).build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocWithInlineSchema() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_inlineSchema.yaml").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withInlineSchemaDepthLevel(1)
                .build();
        Swagger2MarkupConverter.from(file.toURI())
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocGroupedByTags() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);
        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withPathsGroupedBy(GroupBy.TAGS)
                .build();
        Swagger2MarkupConverter.from(file.toURI())
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocGroupedByTagsWithMissingTag() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger_missing_tag.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);
        //When
        try {
            Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                    .withPathsGroupedBy(GroupBy.TAGS)
                    .build();

            Swagger2MarkupConverter.from(file.toURI())
                    .withConfig(config)
                    .build()
                    .intoFolder(outputDirectory.getAbsolutePath());
            // If NullPointerException was not thrown, test would fail the specified message
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (Exception e) {
            assertThat(e).hasMessage("Can't GroupBy.TAGS > Operation 'updatePet' has not tags");
        }
    }

    @Test
    public void testOldSwaggerSpec2AsciiDocConversion() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/error_swagger_12.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConverter.from(file.toURI()).build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocConversionWithDescriptionsAndExamples() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withDefinitionDescriptions(Paths.get("src/docs/asciidoc/definitions"))
                .build();

        Swagger2MarkupConverter.from(file.toURI())
                .withConfig(config).build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocConversionDoesNotContainUriScheme() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_should_not_contain_uri_scheme.yaml").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConverter.from(file.toURI()).build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        assertThat(new String(Files.readAllBytes(new File(outputDirectory, "overview.adoc").toPath())))
                .doesNotContain("=== URI scheme");
    }

    @Test
    public void testSwagger2AsciiDocConversionContainsUriScheme() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/yaml/swagger_should_contain_uri_scheme.yaml").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConverter.from(file.toURI()).build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        assertThat(new String(Files.readAllBytes(new File(outputDirectory, "overview.adoc").toPath())))
                .contains("=== URI scheme");
    }

    @Test
    public void testSwagger2MarkdownConversion() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/markdown/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withMarkupLanguage(MarkupLanguage.MARKDOWN)
                .build();
        Swagger2MarkupConverter.from(file.toURI())
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.md", "overview.md", "paths.md", "security.md"));
    }

    @Test
    public void testSwagger2MarkdownConversionWithDescriptions() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/markdown/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withDefinitionDescriptions(Paths.get("src/docs/markdown/definitions"))
                .withMarkupLanguage(MarkupLanguage.MARKDOWN)
                .build();
        Swagger2MarkupConverter.from(file.toURI())
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(4).containsAll(
                asList("definitions.md", "overview.md", "paths.md", "security.md"));
    }

    @Test
    public void testSwagger2AsciiDocConversionWithSeparatedDefinitions() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withSeparatedDefinitions()
                .build();
        Swagger2MarkupConverter.from(file.toURI()).withConfig(config).build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(5).containsAll(
                asList("definitions", "definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        File definitionsDirectory = new File(outputDirectory, "definitions");
        String[] definitions = definitionsDirectory.list();
        assertThat(definitions).hasSize(6).containsAll(
                asList("identified.adoc", "user.adoc", "category.adoc", "pet.adoc", "tag.adoc", "order.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocConversionWithSeparatedOperations() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withSeparatedOperations()
                .build();
        Swagger2MarkupConverter.from(file.toURI()).withConfig(config).build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(5).containsAll(
                asList("operations", "definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"));

        File pathsDirectory = new File(outputDirectory, "operations");
        String[] paths = pathsDirectory.list();
        assertThat(paths).hasSize(18);
    }

    @Test
    public void testSwagger2MarkdownConversionWithSeparatedDefinitions() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/markdown/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withSeparatedDefinitions()
                .withMarkupLanguage(MarkupLanguage.MARKDOWN)
                .build();
        Swagger2MarkupConverter.from(file.toURI())
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(5).containsAll(
                asList("definitions", "definitions.md", "overview.md", "paths.md", "security.md"));

        File definitionsDirectory = new File(outputDirectory, "definitions");
        String[] definitions = definitionsDirectory.list();
        assertThat(definitions).hasSize(6).containsAll(
                asList("identified.md", "user.md", "category.md", "pet.md", "tag.md", "order.md"));
    }

    @Test
    public void testSwagger2MarkdownConversionHandlesComposition() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/markdown/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withSeparatedDefinitions()
                .withMarkupLanguage(MarkupLanguage.MARKDOWN)
                .build();
        Swagger2MarkupConverter.from(file.toURI())
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory.getAbsolutePath());

        // Then
        String[] directories = outputDirectory.list();
        assertThat(directories).hasSize(5).containsAll(
                asList("definitions", "definitions.md", "overview.md", "paths.md", "security.md"));
        File definitionsDirectory = new File(outputDirectory, "definitions");
        verifyMarkdownContainsFieldsInTables(
                new File(definitionsDirectory, "user.md"),
                ImmutableMap.<String, Set<String>>builder()
                        .put("User", ImmutableSet.of("id", "username", "firstName",
                                "lastName", "email", "password", "phone", "userStatus"))
                        .build()
        );

    }

    @Test
    public void testSwagger2AsciiDocConversionWithRussianOutputLanguage() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withOutputLanguage(Language.RU)
                .build();
        Swagger2MarkupConverter.from(file.toURI())
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        assertThat(new String(Files.readAllBytes(new File(outputDirectory, "definitions.adoc").toPath()),  Charset.forName("UTF-8")))
                .contains("== Определения");
    }

    @Test
    public void testSwagger2AsciiDocExtensions() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .build();
        Swagger2MarkupExtensionRegistry registry = Swagger2MarkupExtensionRegistry.ofEmpty()
                .withExtension(new DynamicDefinitionsContentExtension(Paths.get("src/docs/asciidoc/extensions")))
                .withExtension(new DynamicOperationsContentExtension(Paths.get("src/docs/asciidoc/extensions")))
                .build();
        Swagger2MarkupConverter.from(file.toURI())
                .withConfig(config)
                .withExtensionRegistry(registry)
                .build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        assertThat(new String(Files.readAllBytes(new File(outputDirectory, "paths.adoc").toPath()))).contains(
                "Pet update request extension");
        assertThat(new String(Files.readAllBytes(new File(outputDirectory, "definitions.adoc").toPath()))).contains(
                "Pet extension");

    }

    @Test
    public void testSwagger2MarkdownExtensions() throws IOException {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        File outputDirectory = new File("build/docs/markdown/generated");
        FileUtils.deleteQuietly(outputDirectory);

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withMarkupLanguage(MarkupLanguage.MARKDOWN)
                .build();
        Swagger2MarkupExtensionRegistry registry = Swagger2MarkupExtensionRegistry.ofEmpty()
                .withExtension(new DynamicDefinitionsContentExtension(Paths.get("src/docs/markdown/extensions")))
                .withExtension(new DynamicOperationsContentExtension(Paths.get("src/docs/markdown/extensions")))
                .build();
        Swagger2MarkupConverter.from(file.toURI())
                .withConfig(config)
                .withExtensionRegistry(registry)
                .build()
                .intoFolder(outputDirectory.getAbsolutePath());

        //Then
        assertThat(new String(Files.readAllBytes(new File(outputDirectory, "paths.md").toPath()))).contains(
                "Pet update request extension");
        assertThat(new String(Files.readAllBytes(new File(outputDirectory, "definitions.md").toPath()))).contains(
                "Pet extension");

    }

    @Test
    public void testSwagger2MarkupConfigDefaultPaths() {
        //Given
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withDefinitionDescriptions()
                .withExamples()
                .withOperationDescriptions()
                .withSchemas()
                .build();

        Swagger2MarkupConverter converterBuilder = Swagger2MarkupConverter.from(file.toURI())
                .withConfig(config)
                .build();

        //Then
        URI baseUri = io.github.robwin.swagger2markup.utils.IOUtils.uriParent(converterBuilder.globalContext.swaggerLocation);
        assertThat(converterBuilder.globalContext.config.getDefinitionDescriptionsUri()).isEqualTo(baseUri);
        assertThat(converterBuilder.globalContext.config.getExamplesUri()).isEqualTo(baseUri);
        assertThat(converterBuilder.globalContext.config.getOperationDescriptionsUri()).isEqualTo(baseUri);
        assertThat(converterBuilder.globalContext.config.getSchemasUri()).isEqualTo(baseUri);
    }

    @Test
    public void testSwagger2MarkupConfigDefaultPathsWithUri() {
        //Given

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withDefinitionDescriptions()
                .withExamples()
                .withOperationDescriptions()
                .withSchemas()
                .build();

        Swagger2MarkupConverter converterBuilder = Swagger2MarkupConverter.from(URI.create("http://petstore.swagger.io/v2/swagger.json"))
                .withConfig(config)
                .build();

        //Then
        assertThat(converterBuilder.globalContext.config.getDefinitionDescriptionsUri()).isNull();
        assertThat(converterBuilder.globalContext.config.getExamplesUri()).isNull();
        assertThat(converterBuilder.globalContext.config.getOperationDescriptionsUri()).isNull();
        assertThat(converterBuilder.globalContext.config.getSchemasUri()).isNull();
    }

    @Test
    public void testSwagger2MarkupConfigDefaultPathsWithoutFile() {
        //Given
        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withDefinitionDescriptions()
                .build();

        //Then
        Swagger2MarkupConverter converter = Swagger2MarkupConverter.from(new Swagger())
                .withConfig(config)
                .build();
        assertThat(converter.globalContext.config.isDefinitionDescriptions()).isFalse();
    }

    /**
     * Given a markdown document to search, this checks to see if the specified tables
     * have all of the expected fields listed.
     *
     * @param doc           markdown document file to inspect
     * @param fieldsByTable map of table name (header) to field names expected
     *                      to be found in that table.
     * @throws IOException if the markdown document could not be read
     */
    private static void verifyMarkdownContainsFieldsInTables(File doc, Map<String, Set<String>> fieldsByTable) throws IOException {
        final List<String> lines = Files.readAllLines(doc.toPath(), Charset.defaultCharset());
        final Map<String, Set<String>> fieldsLeftByTable = Maps.newHashMap();
        for (Map.Entry<String, Set<String>> entry : fieldsByTable.entrySet()) {
            fieldsLeftByTable.put(entry.getKey(), Sets.newHashSet(entry.getValue()));
        }
        String inTable = null;
        for (String line : lines) {
            // If we've found every field we care about, quit early
            if (fieldsLeftByTable.isEmpty()) {
                return;
            }

            // Transition to a new table if we encounter a header
            final String currentHeader = getTableHeader(line);
            if (inTable == null || currentHeader != null) {
                inTable = currentHeader;
            }

            // If we're in a table that we care about, inspect this potential table row
            if (inTable != null && fieldsLeftByTable.containsKey(inTable)) {
                // If we're still in a table, read the row and check for the field name
                //  NOTE: If there was at least one pipe, then there's at least 2 fields
                String[] parts = line.split("\\|");
                if (parts.length > 1) {
                    final String fieldName = parts[1];
                    final Set<String> fieldsLeft = fieldsLeftByTable.get(inTable);
                    // Mark the field as found and if this table has no more fields to find,
                    //  remove it from the "fieldsLeftByTable" map to mark the table as done
                    if (fieldsLeft.remove(fieldName) && fieldsLeft.isEmpty()) {
                        fieldsLeftByTable.remove(inTable);
                    }
                }
            }
        }

        // After reading the file, if there were still types, fail
        if (!fieldsLeftByTable.isEmpty()) {
            fail(String.format("Markdown file '%s' did not contain expected fields (by table): %s",
                    doc, fieldsLeftByTable));
        }
    }

    private static String getTableHeader(String line) {
        return line.startsWith("###")
                ? line.replace("###", "").trim()
                : null;
    }

    /*
    @Test
    public void testSwagger2HtmlConversion() throws IOException {
        File file = new File(Swagger2MarkupConverterTest.class.getResource("/json/swagger.json").getFile());
        String asciiDoc =  Swagger2MarkupConverter.from(file.toURI()).build().asString();
        String path = "build/docs/generated/asciidocAsString";
        Files.createDirectories(Paths.get(path));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path, "swagger.adoc"), StandardCharsets.UTF_8)){
            writer.write(asciiDoc);        }
        String asciiDocAsHtml = Asciidoctor.Factory.create().convert(asciiDoc,
                OptionsBuilder.options().backend("html5").headerFooter(true).safe(SafeMode.UNSAFE).docType("book").attributes(AttributesBuilder.attributes()
                        .tableOfContents(true).tableOfContents(Placement.LEFT).sectionNumbers(true).hardbreaks(true).setAnchors(true).attribute("sectlinks")));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path, "swagger.html"), StandardCharsets.UTF_8)){
            writer.write(asciiDocAsHtml);
        }
    }
    */
}
