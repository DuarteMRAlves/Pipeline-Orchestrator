package pipeline.orchestrator.architecture.parsing;

import pipeline.orchestrator.verification.annotations.Verifiable;
import pipeline.orchestrator.verification.annotations.VerifyIterable;
import pipeline.orchestrator.verification.annotations.VerifyNotNull;

import java.util.List;

/**
 * Class with all the definitions for the architecture
 */
@Verifiable
public class ArchitectureInformationDto {

    @VerifyNotNull
    @VerifyIterable
    private List<StageInformationDto> stages;

    @VerifyNotNull
    @VerifyIterable
    private List<LinkInformationDto> links;

    public List<StageInformationDto> getStages() {
        return stages;
    }

    public void setStages(List<StageInformationDto> stages) {
        this.stages = stages;
    }

    public List<LinkInformationDto> getLinks() {
        return links;
    }

    public void setLinks(List<LinkInformationDto> links) {
        this.links = links;
    }
}
