/**
 * 
 */
package org.varunverma.desijokes;

import android.content.SearchRecentSuggestionsProvider;

/**
 * @author Varun
 *
 */
public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {

	final static String AUTHORITY = "org.varunverma.desijokes.SearchSuggestionProvider";
    final static int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}