package fr.inria.corese.command.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;

import fr.inria.corese.command.utils.coresecorowrapper.CoreseSparqlQuery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CoreseSparqlQueryTest {

    @Test
    @DisplayName("Invalid empty SparqlQuery")
    void testInvalidEmptySparqlQuery() {
        String query = "";
        assertFalse(CoreseSparqlQuery.isValidSparqlQuery(query));
    }
}
