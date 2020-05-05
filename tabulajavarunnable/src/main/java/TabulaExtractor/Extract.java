package TabulaExtractor;

import com.google.gson.*;
import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.*;
import technology.tabula.detectors.DetectionAlgorithm;
import technology.tabula.detectors.NurminenDetectionAlgorithm;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.json.TableSerializer;
import technology.tabula.json.TextChunkSerializer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Extract {

    private TabulaParameters tabulaParameters;
    private String base64Data;
    private String password;
    private String extractionMethod;
    private Boolean guess;
    private Boolean useLineReturns;
    private TableExtractor tableExtractor;
    private List<Integer> pages;

    private File pdfTempFile;
    private PDDocument pdfDocument;

    public Extract(){    }

    public Extract(TabulaParameters tabulaParameters, File pdfTempFile, PDDocument pdfDocument){
        this.tabulaParameters = tabulaParameters;
        this.base64Data = tabulaParameters.getBase64Data();
        this.password = tabulaParameters.getPassword();
        this.extractionMethod = tabulaParameters.getExtractionMethod();
        this.guess = Boolean.parseBoolean(tabulaParameters.getGuess());
        this.useLineReturns = Boolean.parseBoolean(tabulaParameters.getUseLineReturns());
        this.pages = Extract.getListOfPages(tabulaParameters.getPagesToExtract());

        this.pdfTempFile = pdfTempFile;
        this.pdfDocument = pdfDocument;

    }

    public String extractTable() {
        String result = "";

        if (!this.pdfTempFile.exists()) {
            try {
                throw new ParseException("File does not exist");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
               result = extractFileTables();

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.deleteIfExists(this.pdfTempFile.toPath());
        } catch (IOException e){
            e.printStackTrace();
        }

        return result;
    }

    public String extractFileTables() throws ParseException {

        String result = extractFile();
        return result;
    }

    private String extractFile() throws ParseException {

        try {
            PageIterator pageIterator = getPageIterator(this.pdfDocument);
            List<Table> tables = new ArrayList<>();

                this.tableExtractor = createExtractor(this.guess, this.extractionMethod, this.useLineReturns);

            while (pageIterator.hasNext()) {
                Page page = pageIterator.next();
                tables.addAll(tableExtractor.extractTables(page));
            }

            Gson gson = gson();
            JsonArray array = new JsonArray();
            Iterator var5 = tables.iterator();

            while (var5.hasNext()) {
                Table table = (Table) var5.next();
                array.add(gson.toJsonTree(table, Table.class));
            }

            return gson.toJson(array);


        } catch (IOException e) {
            throw new ParseException(e.getMessage());
        } finally {
            try {
                if (pdfDocument != null) {
                    pdfDocument.close();
                }
            } catch (IOException e) {
                System.out.println("Error in closing pdf document" + e);
            }
        }
    }

    private PageIterator getPageIterator(PDDocument pdfDocument) throws IOException {
        ObjectExtractor extractor = new ObjectExtractor(pdfDocument);
        return (this.pages == null) ?
                extractor.extract(new ArrayList<Integer>(){{ add(1); }}) :
                extractor.extract(this.pages);
    }

    private static TableExtractor createExtractor(Boolean guessValue, String extractionMethod, Boolean useLineReturns) throws ParseException {
        TableExtractor extractor = new TableExtractor();
        extractor.setGuess(guessValue!=null ? guessValue : false);
        extractor.setUseLineReturns(useLineReturns!=null ? useLineReturns : false);

        List<String> extractOptionsList_1 = Arrays.asList("-r", "--spreadsheet", "-l", "--lattice");
        List<String> extractOptionsList_2 = Arrays.asList("-n", "--no-spreadsheet", "-c", "--columns", "-g", "--guess", "-t", "--stream");

        if(extractionMethod != null && extractOptionsList_1.contains(extractionMethod.toLowerCase())) {
            extractor.setMethod(ExtractionMethod.SPREADSHEET);
        } else if (extractionMethod != null && extractOptionsList_2.contains(extractionMethod.toLowerCase())) {
            extractor.setMethod(ExtractionMethod.BASIC);
        } else {
            extractor.setMethod(ExtractionMethod.DECIDE);
        }

        return extractor;
    }

    private static List<Integer> getIntegersInRange(String range){
        //List<Integer> integersInRange = new ArrayList<>();
        String[] rangeBounds = range.split("-");
        int lowerBound = Integer.parseInt(rangeBounds[0]);
        int upperBound = Integer.parseInt(rangeBounds[1]);
        List<Integer> integersInRange = IntStream.range(lowerBound, upperBound+1).boxed().collect(Collectors.toList());

        return integersInRange;
    }

    private static List<Integer> getListOfPages(String pagesToExtract) {        // pagesToExtract --> eg. "1-4,6,9-23" or "all" or ("" == "1")
        List<Integer> pagesList = new ArrayList<>();
        if (pagesToExtract == null || pagesToExtract.equals("")) {   // get only the first page
            pagesList.add(1);
        } else if (pagesToExtract.equalsIgnoreCase("all")){   // get all pages
            pagesList = null;
        } else {
            if (pagesToExtract.contains(",")){
                String[] pageRanges = pagesToExtract.split(",");
                for (String pageRange :
                        pageRanges) {
                    //pagesList = ListUtils.union(pagesList, getIntegersInRange(pageRange));
                    if(pageRange.contains("-")){
                        pagesList = Stream.concat(pagesList.stream(), Extract.getIntegersInRange(pageRange).stream()).collect(Collectors.toList());
                    } else {
                        pagesList.add(Integer.parseInt(pageRange));
                    }
                }
            } else if (pagesToExtract.contains("-")){
                pagesList = Stream.concat(pagesList.stream(), getIntegersInRange(pagesToExtract).stream()).collect(Collectors.toList());
            } else {
                pagesList.add(Integer.parseInt(pagesToExtract));
            }
        }
        return pagesList;
    }

    private static class TableExtractor {
        private boolean guess = false;
        private boolean useLineReturns = false;
        private BasicExtractionAlgorithm basicExtractor = new BasicExtractionAlgorithm();
        private SpreadsheetExtractionAlgorithm spreadsheetExtractor = new SpreadsheetExtractionAlgorithm();
        private List<Float> verticalRulingPositions = null;
        private ExtractionMethod method = ExtractionMethod.BASIC;

        public TableExtractor() {
        }

        public void setVerticalRulingPositions(List<Float> positions) {
            this.verticalRulingPositions = positions;
        }

        public void setGuess(boolean guess) {
            this.guess = guess;
        }

        public void setUseLineReturns(boolean useLineReturns) {
            this.useLineReturns = useLineReturns;
        }

        public void setMethod(ExtractionMethod method) {
            this.method = method;
        }

        public List<Table> extractTables(Page page) {
            ExtractionMethod effectiveMethod = this.method;
            if (effectiveMethod == ExtractionMethod.DECIDE) {
                effectiveMethod = spreadsheetExtractor.isTabular(page) ?
                        ExtractionMethod.SPREADSHEET :
                        ExtractionMethod.BASIC;
            }
            switch (effectiveMethod) {
                case BASIC:
                    return extractTablesBasic(page);
                case SPREADSHEET:
                    return extractTablesSpreadsheet(page);
                default:
                    return new ArrayList<>();
            }
        }

        public List<Table> extractTablesBasic(Page page) {
            if (guess) {
                // guess the page areas to extract using a detection algorithm
                // currently we only have a detector that uses spreadsheets to find table areas
                DetectionAlgorithm detector = new NurminenDetectionAlgorithm();
                List<Rectangle> guesses = detector.detect(page);
                List<Table> tables = new ArrayList<>();

                for (Rectangle guessRect : guesses) {
                    Page guess = page.getArea(guessRect);
                    tables.addAll(basicExtractor.extract(guess));
                }
                return tables;
            }

            if (verticalRulingPositions != null) {
                return basicExtractor.extract(page, verticalRulingPositions);
            }
            return basicExtractor.extract(page);
        }

        public List<Table> extractTablesSpreadsheet(Page page) {
            // TODO add useLineReturns
            return (List<Table>)spreadsheetExtractor.extract(page);
        }
    }

    private static Gson gson() {
        return (new GsonBuilder())
                .addSerializationExclusionStrategy(new TableSerializerExclusionStrategy())
                .registerTypeAdapter(Table.class, new TableSerializer())
                .registerTypeAdapter(RectangularTextContainer.class, new TextChunkSerializer())
                .registerTypeAdapter(Cell.class, new TextChunkSerializer())
                .registerTypeAdapter(TextChunk.class, new TextChunkSerializer())
                .create();
    }

    private enum ExtractionMethod {
        BASIC,
        SPREADSHEET,
        DECIDE
    }

    public static class TableSerializerExclusionStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipClass(Class<?> arg0) {
            return false;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes fa) {
            return !fa.hasModifier(Modifier.PUBLIC);
        }
    }

}
