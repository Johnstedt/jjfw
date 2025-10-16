import org.jooq.codegen.JavaGenerator;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.TableDefinition;
import org.jooq.meta.ColumnDefinition;
import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import jjfw.meta.JjEntity;

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
            // Add class-level annotation
            processedContent = addEntityAnnotation(processedContent, table);
            // Ensure required imports exist based on used annotations/constants
            processedContent = ensureImports(processedContent);

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

        // Look for annotation patterns like @JsonProperty(...), @Ignore(...), etc.
        Pattern annotationPattern = Pattern.compile("@[A-Za-z][A-Za-z0-9_.]*(?:\\([^)]*\\))?");
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
        // Pattern to find field declarations: private Type fieldName;
        String fieldPattern = "(\\s*)(private\\s+\\w+(?:<[^>]*>)?\\s+" + Pattern.quote(fieldName) + "\\s*;)";
        // Find field declaration at the start of a line for precise insertion
        //String fieldPattern = "(?m)^(\\s*)(private\\s+[_$A-Za-z][_$A-Za-z0-9]*(?:<[^>]*>)?\\s+" + Pattern.quote(fieldName) + "\\s*;)";
        Pattern pattern = Pattern.compile(fieldPattern, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String indentation = matcher.group(1);

            System.out.println("Indentation");
            System.out.println(indentation);

            String fieldDeclaration = matcher.group(2);
            System.out.println("Field declaration");
            System.out.println(fieldDeclaration);

            // Replace with annotations + field declaration
            // Add newline after annotation and use same indentation for field
            String replacement = indentation + annotations + "\n" + indentation.replaceAll("\n", "") + fieldDeclaration;

            System.out.println("Replacement");
            System.out.println(replacement);


            content = matcher.replaceFirst(Matcher.quoteReplacement(replacement));
        }

        return content;
    }

    private String addEntityAnnotation(String content, TableDefinition table) {
        // If already contains @JjEntity, skip
        if (content.contains("@JjEntity")) return content;
        String tableComment = table.getComment();
        String annotationFromComment = null;
        if (tableComment != null) {
            Matcher m = Pattern.compile("@JjEntity\\([^)]*\\)").matcher(tableComment);
            if (m.find()) {
                annotationFromComment = m.group();
            }
        }
        String tableName = table.getName();
        String annotation = annotationFromComment != null ? annotationFromComment : "@JjEntity(table=\"" + tableName + "\")";
        // Insert import if missing
        if (!content.contains("jjfw.meta.JjEntity")) {
            content = content.replaceFirst("(package \\S+;\\s+)", "$1import jjfw.meta.JjEntity;\n");
        }
        // Inject before class declaration
        content = content.replaceFirst("(public class )", annotation + "\n$1");
        return content;
    }

    private String ensureImports(String content) {
        boolean usesJsonProperty = content.contains("@JsonProperty");
        boolean usesIgnore = content.contains("@Ignore");

        // Add class imports if annotations are used
        if (usesJsonProperty && !content.contains("import com.fasterxml.jackson.annotation.JsonProperty;")) {
            content = content.replaceFirst("(package \\S+;\\s+)", "$1import com.fasterxml.jackson.annotation.JsonProperty;\n");
        }
        if (usesIgnore && !content.contains("import jjfw.annotations.Ignore;")) {
            content = content.replaceFirst("(package \\S+;\\s+)", "$1import jjfw.annotations.Ignore;\n");
        }

        // Static imports for common constants, only if the corresponding annotation is present
        if (usesJsonProperty && content.matches("(?s).*\\bREAD_ONLY\\b.*")
                && !content.contains("import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;")) {
            content = content.replaceFirst("(package \\S+;\\s+)", "$1import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;\n");
        }
        if (usesJsonProperty && content.matches("(?s).*\\bWRITE_ONLY\\b.*")
                && !content.contains("import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;")) {
            content = content.replaceFirst("(package \\S+;\\s+)", "$1import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;\n");
        }
        if (usesJsonProperty && content.matches("(?s).*\\bREAD_WRITE\\b.*")
                && !content.contains("import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_WRITE;")) {
            content = content.replaceFirst("(package \\S+;\\s+)", "$1import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_WRITE;\n");
        }

        if (usesIgnore && content.matches("(?s).*\\bCREATE\\b.*")
                && !content.contains("import static jjfw.annotations.Ignore.Operation.CREATE;")) {
            content = content.replaceFirst("(package \\S+;\\s+)", "$1import static jjfw.annotations.Ignore.Operation.CREATE;\n");
        }
        if (usesIgnore && content.matches("(?s).*\\bUPDATE\\b.*")
                && !content.contains("import static jjfw.annotations.Ignore.Operation.UPDATE;")) {
            content = content.replaceFirst("(package \\S+;\\s+)", "$1import static jjfw.annotations.Ignore.Operation.UPDATE;\n");
        }
        if (usesIgnore && content.matches("(?s).*\\bREAD\\b.*")
                && !content.contains("import static jjfw.annotations.Ignore.Operation.READ;")) {
            content = content.replaceFirst("(package \\S+;\\s+)", "$1import static jjfw.annotations.Ignore.Operation.READ;\n");
        }

        return content;
    }
}
