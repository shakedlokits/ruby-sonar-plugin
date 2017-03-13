package com.godaddy.sonar.ruby.rubocop.parsing.impl;

import com.godaddy.sonar.ruby.rubocop.data.*;
import com.godaddy.sonar.ruby.rubocop.parsing.ReportJsonParser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergio on 3/13/17.
 */
public class DefaultReportJsonParser implements ReportJsonParser {
    private static final Logger LOG = Loggers.get(DefaultReportJsonParser.class);

    @Override
    public Report parse(java.io.File file) throws IOException {
        LOG.info("Starting parsing Rubocop report JSON file.");
        String fileString = FileUtils.readFileToString(file, "UTF-8");
        JsonParser parser = new JsonParser();
        JsonObject resultJsonObject = parser.parse(fileString).getAsJsonObject();
        return parseJsonReport(resultJsonObject);
    }

    private Report parseJsonReport(JsonObject jsonObject) {
        LOG.info("parseJsonReport called");
        Report report = new Report();
        JsonObject metadataJsonObject = jsonObject.getAsJsonObject("metadata");
        report.setMetadata(parseMetadata(metadataJsonObject));
        JsonArray filesJsonArray = jsonObject.getAsJsonArray("files");
        report.setFiles(parseFiles(filesJsonArray));
        return report;
    }

    private Metadata parseMetadata(JsonObject jsonObject) {
        LOG.info("parseMetadata called");
        Metadata metadata = new Metadata();
        metadata.setRubocopVersion(jsonObject.get("rubocop_version").getAsString());
        metadata.setRubyEngine(jsonObject.get("ruby_engine").getAsString());
        metadata.setRubyVersion(jsonObject.get("ruby_version").getAsString());
        metadata.setRubyPatchlevel(jsonObject.get("ruby_patchlevel").getAsString());
        metadata.setRubyPlatform(jsonObject.get("ruby_platform").getAsString());
        return metadata;
    }

    private List<File> parseFiles(JsonArray jsonArray) {
        LOG.info("parseFiles called. There are " + jsonArray.size() + " files in the report.");
        List<File> files = new ArrayList<>();
        for (JsonElement fileJsonElement : jsonArray) {
            files.add(parseFile((JsonObject)fileJsonElement));
        }
        return files;
    }

    private File parseFile(JsonObject jsonObject) {
        JsonArray offensesJsonArray = jsonObject.getAsJsonArray("offenses");
        File file = new File();
        file.setPath(jsonObject.get("path").getAsString());
        file.setOffenses(parseOffenses(offensesJsonArray));
        LOG.info("Parsed information for " + file.getPath());
        return file;
    }

    private List<Offense> parseOffenses(JsonArray jsonArray) {
        List<Offense> offenses = new ArrayList<>();
        for(JsonElement offenseJsonElement : jsonArray) {
            offenses.add(parseOffense((JsonObject)offenseJsonElement));
        }
        return offenses;
    }

    private Offense parseOffense(JsonObject jsonObject) {
        Offense offense = new Offense();
        offense.setSeverity(jsonObject.get("severity").getAsString());
        offense.setMessage(jsonObject.get("message").getAsString());
        offense.setCopName(jsonObject.get("cop_name").getAsString());
        offense.setCorrected(jsonObject.get("corrected").getAsBoolean());
        offense.setLocation(parseLocation(jsonObject.get("location").getAsJsonObject()));
        return offense;
    }

    private Location parseLocation(JsonObject jsonObject) {
        Location location = new Location();
        location.setColumn(jsonObject.get("column").getAsInt());
        location.setLine(jsonObject.get("line").getAsInt());
        location.setLength(jsonObject.get("length").getAsInt());
        return location;
    }
}
