package com.godaddy.sonar.ruby.rubocop;

import com.godaddy.sonar.ruby.RubyPlugin;
import com.godaddy.sonar.ruby.core.Ruby;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

import java.io.InputStream;

/**
 * Created by sergio on 3/13/17.
 */
public class CopsRulesDefinition implements RulesDefinition {
    private static final String RULES_DEFINITION_XML_RESOURCE_PATH = "/com/godaddy/sonar/ruby/rubocop/RulesRepository.xml";

    private final RulesDefinitionXmlLoader xmlLoader;

    public CopsRulesDefinition(RulesDefinitionXmlLoader xmlLoader) {
        this.xmlLoader = xmlLoader;
    }

    @Override
    public void define(Context context) {
        NewRepository repository = createRepository(context);
        loadXmlRulesDefinitions(repository);
        repository.done();
    }

    private NewRepository createRepository(Context context) {
        return context
                .createRepository(RubyPlugin.KEY_REPOSITORY_RUBOCOP, Ruby.KEY)
                .setName(RubyPlugin.NAME_REPOSITORY_RUBOCOP);
    }

    private void loadXmlRulesDefinitions(NewRepository repository) {
        InputStream inputStream = getClass().getResourceAsStream(RULES_DEFINITION_XML_RESOURCE_PATH);
        xmlLoader.load(repository, inputStream, "UTF-8");
    }
}
