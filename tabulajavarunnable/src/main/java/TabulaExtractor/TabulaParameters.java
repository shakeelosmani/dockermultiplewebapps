package TabulaExtractor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TabulaParameters {
    @JsonProperty("base64Data")
    private String base64Data;
    @JsonProperty("pagesToExtract")
    private String pagesToExtract;
    @JsonProperty("extractionMethod")
    private String extractionMethod;
    @JsonProperty("outputFormat")
    private String outputFormat;
    @JsonProperty("password")
    private String password;
    @JsonProperty("guess")
    private String guess;
    @JsonProperty("useLineReturns")
    private String useLineReturns;


    public TabulaParameters(){}

    public String getBase64Data() {
        return base64Data;
    }

    public String getPagesToExtract() {
        return pagesToExtract;
    }

    public String getExtractionMethod() { return extractionMethod; }

    public String getOutputFormat() {
        return outputFormat;
    }

    public String getPassword() { return password; }

    public String getGuess() { return guess; }

    public String getUseLineReturns() { return useLineReturns; }
}
