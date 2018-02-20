package com.godaddy.sonar.ruby.core;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.AbstractLanguage;

/**
 * Ruby language implementation
 */
public class LanguageRuby extends AbstractLanguage {

    public static final LanguageRuby INSTANCE = new LanguageRuby("ruby");

    /**
     * Java key
     */
    public static final String KEY = "ruby";

    /**
     * Java name
     */
    public static final String NAME = "Ruby";

    /**
     * Default package name for classes without package def
     */
    public static final String DEFAULT_PACKAGE_NAME = "[default]";

    /**
     * Java files knows suffixes
     */
    public static final String[] SUFFIXES = {".rb", ".ruby"};


    public LanguageRuby(String key) {
        super(key);
    }

    /**
     * Default constructor
     */
    public LanguageRuby() {
        super(KEY, NAME);
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractLanguage#getFileSuffixes()
     */
    public String[] getFileSuffixes() {
        return SUFFIXES;
    }

    public static boolean isRubyFile(java.io.File file) {
        String suffix = "." + StringUtils.lowerCase(StringUtils.substringAfterLast(file.getName(), "."));
        return ArrayUtils.contains(SUFFIXES, suffix);
    }

}
