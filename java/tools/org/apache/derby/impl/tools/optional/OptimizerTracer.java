/*

   Derby - Class org.apache.derby.impl.tools.optional.OptimizerTracer

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package org.apache.derby.impl.tools.optional;

import java.io.PrintWriter;
import java.sql.SQLException;

import org.apache.derby.iapi.db.OptimizerTrace;
import org.apache.derby.iapi.sql.compile.OptTrace;
import org.apache.derby.iapi.sql.dictionary.OptionalTool;

/**
 * <p>
 * OptionalTool to create wrapper functions and views for all of the user tables
 * in a foreign database.
 * </p>
 */
public	class   OptimizerTracer  implements OptionalTool
{
    ////////////////////////////////////////////////////////////////////////
    //
    //	CONSTANTS
    //
    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    //
    //	STATE
    //
    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    //
    //	CONSTRUCTOR
    //
    ////////////////////////////////////////////////////////////////////////

    /** 0-arg constructor required by the OptionalTool contract */
    public  OptimizerTracer() {}

    ///////////////////////////////////////////////////////////////////////////////////
    //
    // OptionalTool BEHAVIOR
    //
    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Turns on optimizer tracing.
     * </p>
     */
    public  void    loadTool( String... configurationParameters )
        throws SQLException
    {
        OptimizerTrace.setOptimizerTrace( true );
    }

    /**
     * <p>
     * Print the optimizer trace and turn off tracing. Takes optional parameters:
     * </p>
     *
     * <ul>
     * <li><b>fileName</b> - Where to write the optimizer trace. If omitted, the trace is written to System.out.</li>
     * </ul>
     */
    public  void    unloadTool( String... configurationParameters )
        throws SQLException
    {
        try {
            OptTrace    tracer = OptimizerTrace.getOptimizerTracer();
            boolean     needsClosing = false;

            PrintWriter pw;
            if (
                (configurationParameters != null) &&
                (configurationParameters.length > 0)
                )
            {
                pw = new PrintWriter( configurationParameters[ 0 ] );
                needsClosing = true;
            }
            else { pw = new PrintWriter( System.out ); }
        
            if ( tracer != null )
            {
                tracer.printToWriter( pw );
                pw.flush();
            }

            if ( needsClosing ) { pw.close(); }
        }
        catch (Exception e) { throw wrap( e ); }
        finally
        {
            OptimizerTrace.setOptimizerTracer( null );
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //
    //	MINIONS
    //
    ////////////////////////////////////////////////////////////////////////

    /** Wrap an exception in a SQLException */
    private SQLException    wrap( Throwable t )
    {
        return new SQLException( t.getMessage(), t );
    }
}

