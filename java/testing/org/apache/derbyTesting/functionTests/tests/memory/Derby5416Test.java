/**
 * Derby - Class org.apache.derbyTesting.functionTests.tests.memory.Derby5416Test
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.derbyTesting.functionTests.tests.memory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import junit.framework.Test;
import org.apache.derbyTesting.junit.BaseJDBCTestCase;
import org.apache.derbyTesting.junit.CleanDatabaseTestSetup;
import org.apache.derbyTesting.junit.SystemPropertyTestSetup;
import org.apache.derbyTesting.junit.TestConfiguration;

/**
 * Regression test case for DERBY-5416. If the heap is full of objects that
 * are eligible for garbage collection when SYSCS_COMPRESS_TABLE is called,
 * the sort buffer may end up taking too much space and make the compression
 * fail with an OutOfMemoryError. The test should run with 16 MB heap space
 * in order to reproduce the bug.
 */
public class Derby5416Test extends BaseJDBCTestCase {

    private static final int NUM_ROWS = 10000;
    private static final int SIZE_COLUMN_ENTRY = 50;
    private static final int SIZE_GARBAGE_ENTRY = 10 * 1024;
    private static final int MAX_GARBAGE = 16 * 1024 * 1024;

    public Derby5416Test(String name) {
        super(name);
    }

    public static Test suite() {
        // Run with a small page cache so that we don't run out of heap
        // space while populating the test table (each page is larger than
        // normal because the test table has many long columns).
        return SystemPropertyTestSetup.singleProperty(
                new CleanDatabaseTestSetup(
                        TestConfiguration.embeddedSuite(Derby5416Test.class)),
                "derby.storage.pageCacheSize", "40", true);
    }

    public void testOOME() throws SQLException {
        setAutoCommit(false);
        Statement s = createStatement();

        println("Create table");
        s.execute("create table D5416(id int, a varchar(32672), "
                + "b varchar(32672), c varchar(32672), d varchar(32672), "
                + "e varchar(32672), f varchar(32672), g varchar(32672), "
                + "h varchar(32672))");

        println("Fill table");
        PreparedStatement ps = prepareStatement(
                "insert into D5416 values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
        int[] bogusData = new int[SIZE_COLUMN_ENTRY];
        for (int i = 0; i <= NUM_ROWS; i++) {
            ps.setInt(1, i);
            for (int j = 2; j <= 9; j++) {
                Arrays.fill(bogusData, i + j);
                ps.setString(j, Arrays.toString(bogusData));
            }
            ps.execute();
            if (i % 1000 == 0) {
                println((i / 1000) + "/" + (NUM_ROWS / 1000));
            }
        }
        ps.close();

        println("Create index");
        s.execute("create index D5416_I on D5416(id, a, b, c, d, e, f, g, h)");

        println("Fill heap");
        ArrayList<int[]> garbage = new ArrayList<int[]>();
        try {
            // Loop until we get an OOME, or we have added more than 16 MB
            // worth of garbage (which indicates that the test runs with larger
            // than 16 MB heap, in which case it is unlikely to reproduce the
            // bug).
            int totalGarbage = 0;
            while (totalGarbage < MAX_GARBAGE) {
                garbage.add(new int[SIZE_GARBAGE_ENTRY]);
                // Each int in the garbage entry takes 4 bytes.
                totalGarbage += SIZE_GARBAGE_ENTRY * 4;
            }
            println("Not enough garbage to fill the heap. "
                    + "Continuing test anyway.");
        } catch (OutOfMemoryError oom) {
            // release the heap
            garbage = null;
        }

        // Immediately call the compress in the hope that the gc didn't
        // hit us. This is likely to fail with an OutOfMemoryError until
        // DERBY-5416 is fixed.
        println("Run compress");
        s.execute("call syscs_util.syscs_compress_table('APP', 'D5416', 1)");
    }
}
