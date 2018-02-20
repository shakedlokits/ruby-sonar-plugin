package com.godaddy.sonar.ruby.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.Scopes;
import org.sonar.api.utils.WildcardPattern;

@SuppressWarnings("rawtypes")
public class RubyPackage extends Resource {
    private static final long serialVersionUID = -8901912464767594618L;
    public static final String DEFAULT_PACKAGE_NAME = "[default]";

    public RubyPackage(String key) {
        super();
        setKey(StringUtils.defaultIfEmpty(StringUtils.trim(key), DEFAULT_PACKAGE_NAME));
    }

    public boolean matchFilePattern(String antPattern) {
        String patternWithoutFileSuffix = StringUtils.substringBeforeLast(antPattern, ".");
        WildcardPattern matcher = WildcardPattern.create(patternWithoutFileSuffix, ".");
        return matcher.match(getKey());
    }

    public String getDescription() {
        return null;
    }

    public String getName() {
        return getKey();
    }

    public Resource getParent() {
        return null;
    }

    public String getLongName() {
        return null;
    }

    public Language getLanguage() {
        return Ruby.INSTANCE;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("key", getKey()).toString();
    }

    @Override
    public String getScope() {
        return Scopes.DIRECTORY;
    }

    @Override
    public String getQualifier() {
        return Qualifiers.PROJECT;
    }
}
