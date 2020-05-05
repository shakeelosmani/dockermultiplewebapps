package Controllers;


import TabulaExtractor.Extract;
//import TabulaExtractor.ExtractApp;
import TabulaExtractor.TabulaParameters;
import Util.CustomErrorMessageHandler;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;

@RestController @RequestMapping("/")
public class ExtractController {

    @PostMapping("/pdftablestojson")
    public ResponseEntity<?> index(@RequestBody TabulaParameters jsonInput) {

        File pdfTempFile = null;
        PDDocument pdfDocument = null;

        if (jsonInput!=null) {
            CustomErrorMessageHandler errorMessageHandler = new CustomErrorMessageHandler();

            //1. Make sure base64 content is not null or empty
            if (jsonInput.getBase64Data()==null || jsonInput.getBase64Data().isEmpty()) {
                errorMessageHandler.fieldName = "base64Data";
                errorMessageHandler.message = "Missing mandatory parameter. The request body must contain a key 'base64Data' and base64 format.\n" +
                        "It is the base64 format of the content of the PDF file from which tables are to be extracted.\n";

                return ResponseEntity.badRequest().body(errorMessageHandler);

            }

            //2. Check if content sent is of type PDF
            byte[] bytes = Base64.getDecoder().decode(jsonInput.getBase64Data());
            String filename = UUID.randomUUID().toString() + ".pdf";
            try (DataOutputStream os = new DataOutputStream(new FileOutputStream(filename))) {

                os.write(bytes);
                os.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            pdfTempFile = new File(filename);

            try {
                pdfDocument = jsonInput.getPassword() == null ? PDDocument.load(pdfTempFile) : PDDocument.load(pdfTempFile, jsonInput.getPassword());
            } catch (IOException e) {
                errorMessageHandler.fieldName = "file content error";
                errorMessageHandler.message = "File content is either corrupt or is not of PDF type : " + e.toString();


                // 1. Close PDF document
                try {
                    if (pdfDocument != null) {
                        pdfDocument.close();
                    }
                } catch (IOException e2) {
                    System.out.println("Error in closing pdf document" + e2);
                }

                // 2. Delete file
                try {
                    Files.deleteIfExists(pdfTempFile.toPath());
                } catch (IOException e3) {
                    e.printStackTrace();
                }
            }


        }

        Extract extract = new Extract(jsonInput, pdfTempFile, pdfDocument);
        String json = extract.extractTable();

        return ResponseEntity.ok().body(json);
    }

}
