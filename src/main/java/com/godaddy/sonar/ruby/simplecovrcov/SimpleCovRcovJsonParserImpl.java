package com.godaddy.sonar.ruby.simplecovrcov;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SimpleCovRcovJsonParserImpl implements SimpleCovRcovJsonParser {
    private static final Logger LOG = Loggers.get(SimpleCovRcovJsonParserImpl.class);

    public Map<String, CoverageMeasuresBuilder> parse(File file) throws IOException {
        Map<String, CoverageMeasuresBuilder> coveredFiles = Maps.newHashMap();

        String fileString = FileUtils.readFileToString(file, "UTF-8");

        JsonParser parser = new JsonParser();
        JsonObject resultJsonObject = parser.parse(fileString).getAsJsonObject();

        String coverageRootNode = "";
        if(resultJsonObject.get("RSpec") != null) { coverageRootNode = "RSpec"; }
        if(resultJsonObject.get("MiniTest") != null) { coverageRootNode = "MiniTest"; }

        if(coverageRootNode.equals("")) { return coveredFiles; }

        JsonObject coverageJsonObj = resultJsonObject.get(coverageRootNode).getAsJsonObject().get("coverage").getAsJsonObject();

        // for each file in the coverage report
        for (int j = 0; j < coverageJsonObj.entrySet().size(); j++) {
            CoverageMeasuresBuilder fileCoverage = CoverageMeasuresBuilder.create();

            String filePath = ((Map.Entry) coverageJsonObj.entrySet().toArray()[j]).getKey().toString();
            LOG.debug("filePath " + filePath);

            JsonArray coverageArray = coverageJsonObj.get(filePath).getAsJsonArray();

            // for each line in the coverage array
            for (int i = 0; i < coverageArray.size(); i++) {
                Long line = null;

                if (!coverageArray.get(i).isJsonNull()) {
                    line = coverageArray.get(i).getAsLong();
                }

                Integer intLine = 0;
                int lineNumber = i + 1;
                if (line != null) {
                    intLine = line.intValue();
                    fileCoverage.setHits(lineNumber, intLine);
                }
            }
            LOG.info("FILE COVERAGE = " + fileCoverage.getCoveredLines());
            coveredFiles.put(filePath, fileCoverage);
        }
        return coveredFiles;
    }
}
