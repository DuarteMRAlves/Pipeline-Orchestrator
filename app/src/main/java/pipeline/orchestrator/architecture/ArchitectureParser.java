package pipeline.orchestrator.architecture;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.graph.*;
import pipeline.core.common.utils.Conditions;

import java.io.*;
import java.util.HashMap;

public class ArchitectureParser {

    private static final Splitter SPLITTER = Splitter.on(',')
            .trimResults();

    /**
     * Parses the system architecture from configuration files
     * @param stagesInfoFile csv file with services information where each line is in the format:
     *                       id, host, port
     * @param stagesLinksFile csv file with links information where each line is in the format:
     *                        source_id, target_id, source_field_name, target_field_name
     *                        (last two are present  in the case of splitting or merging and empty otherwise)
     * @return the graph with each stage information and the corresponding links
     */
    public static ImmutableValueGraph<StageInformation, LinkInformation> parse(
            String stagesInfoFile,
            String stagesLinksFile)
            throws IOException {

        HashMap<Integer, StageInformation> stagesMapping =
                parseStagesInfo(stagesInfoFile);

        return parseStagesLinks(stagesMapping, stagesLinksFile);
    }

    private static HashMap<Integer, StageInformation> parseStagesInfo(
            String stagesInfoFile)
            throws IOException {

        HashMap<Integer, StageInformation> stageInformationMapping = new HashMap<>();

        // Run inside try block to automatically close resource
        try (BufferedReader fileReader = new BufferedReader(
                new FileReader(new File(stagesInfoFile)))) {

            String line;
            StageInformation.Builder builder = StageInformation.newBuilder();

            while ((line = fileReader.readLine()) != null) {
                String[] tokens = line.split(",");
                Preconditions.checkState(
                        tokens.length == 3 || tokens.length == 4);
                builder.clear()
                        .setServiceHost(tokens[1])
                        .setServicePort(Integer.parseInt(tokens[2]));

                if (tokens.length == 4) builder.setMethodName(tokens[3]);

                int stageId = Integer.parseInt(tokens[0]);
                StageInformation stageInfo = builder.build();

                Conditions.checkState(
                        stageInformationMapping.put(stageId, stageInfo) == null,
                        IllegalStateException::new);
            }
        }

        return stageInformationMapping;
    }

    private static ImmutableValueGraph<StageInformation, LinkInformation> parseStagesLinks(
            HashMap<Integer, StageInformation> stagesMapping,
            String serviceStagesLinksFile)
            throws IOException {

        MutableValueGraph<StageInformation, LinkInformation> graph = ValueGraphBuilder.directed()
                .allowsSelfLoops(false)
                .build();

        // Run inside try block to automatically close resource
        try (BufferedReader fileReader = new BufferedReader(
                new FileReader(new File(serviceStagesLinksFile)))) {

            String line;
            while ((line = fileReader.readLine()) != null) {
                String[] tokens = SPLITTER.splitToList(line).toArray(new String[0]);

                Preconditions.checkState(tokens.length == 4);

                Integer sourceId = Integer.parseInt(tokens[0]);
                Integer targetId = Integer.parseInt(tokens[1]);
                String sourceFieldName = tokens[2];
                String targetFieldName = tokens[3];

                LinkInformation linkInformation = LinkInformation.newBuilder()
                        .setSourceFieldName(sourceFieldName)
                        .setTargetFieldName(targetFieldName)
                        .build();

                // Edge value has the form sourceFieldName, destFieldName
                // The first value will be an outputField for source and
                // the second will be an input field for destination
                graph.putEdgeValue(
                        stagesMapping.get(sourceId),
                        stagesMapping.get(targetId),
                        linkInformation);
            }
        }

        return ImmutableValueGraph.copyOf(graph);
    }
}
