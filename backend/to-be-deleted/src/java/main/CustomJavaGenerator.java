package Main;

import org.jooq.codegen.JavaGenerator;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.TableDefinition;
import org.jooq.meta.ColumnDefinition;
import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CustomJavaGenerator extends JavaGenerator {

    @Override
    protected void generatePojo(TableDefinition table, JavaWriter out) {
        // Create a temporary file to capture the generated POJO
        try {
            File tempFile = File.createTempFile("pojo_", ".java");
            tempFile.deleteOnExit();

            // Create a new JavaWriter for the temp file
            JavaWriter captureWriter = newJavaWriter(tempFile);

            // Call the parent method to generate the standard POJO
            super.generatePojo(table, captureWriter);
            captureWriter.close();

            // Read the generated content
            String generatedContent = java.nio.file.Files.readString(tempFile.toPath());

            // Post-process the content to add custom annotations
            String processedContent = addCustomAnnotations(generatedContent, table);

            // Write the processed content to the actual output
            out.print(processedContent);

        } catch (Exception e) {
            // Fallback to original generation if something goes wrong
            super.generatePojo(table, out);
        }
    }

    private String addCustomAnnotations(String generatedContent, TableDefinition table) {
        String result = generatedContent;

        // Process each column to check for custom annotations in comments
        for (ColumnDefinition column : table.getColumns()) {
            String comment = column.getComment();
            if (comment != null && !comment.trim().isEmpty()) {
                // Extract custom annotations from the comment
                String annotations = extractAnnotationsFromComment(comment);
                if (!annotations.isEmpty()) {
                    // Add annotations to the field using the Java member name
                    String fieldName = getStrategy().getJavaMemberName(column, null);
                    result = addAnnotationsToField(result, fieldName, annotations);
                }
            }
        }

        return result;
    }

    private String extractAnnotationsFromComment(String comment) {
        StringBuilder annotations = new StringBuilder();

        // Look for annotation patterns like @Api.exclude, @JsonIgnore, etc.
        Pattern annotationPattern = Pattern.compile("@[A-Za-z][A-Za-z0-9_.]*(?:\\([^)]*\\))?");
        System.out.println("Extracting annotations from comment: " + comment);

        Matcher matcher = annotationPattern.matcher(comment);

        while (matcher.find()) {
            String annotation = matcher.group().trim();
            System.out.println("Found annotation: " + annotation);
            // Don't add indentation here - it will be handled in addAnnotationsToField
            annotations.append(annotation);
        }

        return annotations.toString();
    }

    private String addAnnotationsToField(String content, String fieldName, String annotations) {
        // Pattern to find field declarations
        // This looks for "private Type fieldName;" pattern
        String fieldPattern = "(\\s*)(private\\s+\\w+(?:<[^>]*>)?\\s+" + Pattern.quote(fieldName) + "\\s*;)";
        Pattern pattern = Pattern.compile(fieldPattern, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String indentation = matcher.group(1);
            String fieldDeclaration = matcher.group(2);

            // Replace with annotations + field declaration
            // Add newline after annotation and use same indentation for field
            String replacement = indentation + annotations + "\n" + indentation + fieldDeclaration;
            content = matcher.replaceFirst(Matcher.quoteReplacement(replacement));
        }

        return content;
    }
}
