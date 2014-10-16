/*
 * Copyright 2013 uaiHebert Solucoes em Informatica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * */
package com.uaihebert.test;

import com.uaihebert.model.test.RegularEntityOne;
import com.uaihebert.uaicriteria.UaiCriteria;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static org.junit.Assert.*;

public class TupleAbstractTest extends AbstractTest {
    protected enum OpenJpaIndexToConvert {
        NONE, SECOND
    }

    protected <E> E extractResult(final UaiCriteria<RegularEntityOne> uaiCriteria, final Class<E> resultType) {
        final E criteriaResult;

        if (isEclipselink()) {
            final Vector resultAsVector = (Vector) uaiCriteria.getTupleResult();
            criteriaResult = (E) resultAsVector.get(0);
        } else {
            final List resultAsList = uaiCriteria.getTupleResult();
            criteriaResult = (E) resultAsList.get(0);
        }

        return criteriaResult;
    }

    protected void validateResultWithVector(final String query, final UaiCriteria<RegularEntityOne> uaiCriteria) {
        final List<Object> resultFromJPQL = jpqlHelper.getListFromJPQL(query, Object.class);
        final Vector jpqlVector = (Vector) resultFromJPQL;

        assertTrue("making sure that the tuple worked", jpqlVector.size() > 1);

        final Vector tupleVector = (Vector) uaiCriteria.getTupleResult();

        assertEquals("making sure that the tuple has the same value", jpqlVector.size(), tupleVector.size());

        validateListResult(jpqlVector, tupleVector);
    }

    protected void validateListResult(final List jpqlVector, final List tupleVector, final OpenJpaIndexToConvert... convertIndex) {
        for (int i = 0; i < jpqlVector.size(); i++) {
            final Object[] jpqlLine = (Object[]) jpqlVector.get(i);
            final Object[] tupleLine = (Object[]) tupleVector.get(i);

            if (isOpenJPA()) {
                // open JPA is returning long/int values in the avg attributes
                if (convertIndex.length == 0 || OpenJpaIndexToConvert.NONE.equals(convertIndex)) {
                    // for some reason is returning long for jpql and int for criteria
                    assertEquals(jpqlLine[0], tupleLine[0]);
                    try {
                        // for jpql is returning long and criteria big decimal
                        assertEquals(new BigDecimal(jpqlLine[0].toString()), new BigDecimal(tupleLine[0].toString()));
                    } catch (final NumberFormatException nfe) {
                        try {
                            assertEquals(jpqlLine[0], tupleLine[0]);
                        } catch (final Exception ex) {
                            fail("Could not parse query result");
                        }
                    }
                    continue;
                }

                if (convertIndex.length > 0 && i == 1 && Arrays.asList(convertIndex).contains(OpenJpaIndexToConvert.SECOND)) {
                    assertEquals(Double.valueOf(jpqlLine[1].toString()), Double.valueOf(tupleLine[1].toString()));
                }
            } else {
                // for some reason the eclipslink is returning null values
                if (jpqlLine[1] == null && tupleLine[1] == null) {
                    continue;
                }

                try {
                    // for jpql is returning long and criteria big decimal
                    assertEquals(new BigDecimal(jpqlLine[1].toString()), new BigDecimal(tupleLine[1].toString()));
                } catch (final NumberFormatException nfe) {
                    try {
                        assertEquals(jpqlLine[1], tupleLine[1]);
                    } catch (final Exception ex) {
                        fail("Could not parse query result");
                    }
                }
            }
        }
    }

    protected void validateResultAsList(final String query, final UaiCriteria<RegularEntityOne> uaiCriteria) {
        final List jpqlList = jpqlHelper.getListFromJPQL(query, Object[].class);

        assertTrue("making sure that the sum worked", jpqlList.size() > 1);

        final List tupleList = uaiCriteria.getTupleResult();

        assertEquals("making sure that the sum has the same value", jpqlList.size(), tupleList.size());

        validateListResult(jpqlList, tupleList);
    }
}
