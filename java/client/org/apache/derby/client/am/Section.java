/*
 *
 * Derby - Class org.apache.derby.client.am.Section
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package org.apache.derby.client.am;

public class Section {

    protected int sectionNumber;
    protected String packageName;
    protected String serverCursorName; // As given by dnc package set
    int resultSetHoldability_;

    // Stores the package name and consistency token
    byte[] PKGNAMCBytes;
    boolean isGenerated; // flag to identify server generated sections

    public Section(Agent agent, String name, int sectionNumber, String cursorName, int resultSetHoldability) {
        // default for all sections except for generated section , isGenerated is set to false
        init(agent, name, sectionNumber, cursorName, resultSetHoldability, false);
    }

    public Section(Agent agent, String name, int sectionNumber, String cursorName, int resultSetHoldability, boolean isGenerated) {
        init(agent, name, sectionNumber, cursorName, resultSetHoldability, isGenerated);
    }

    private void init(Agent agent, String name, int sectionNumber, String cursorName, int resultSetHoldability, boolean isGenerated) {
        this.packageName = name;
        this.sectionNumber = sectionNumber;
        this.serverCursorName = cursorName;
        resultSetHoldability_ = resultSetHoldability;
        agent_ = agent;
        this.isGenerated = isGenerated;

        // Store the packagename and consistency token bytes depending on the holdability
        // PKGNAMCBytes will point to the appropriate byte array in SectionManager
        // that stores the PKGNAMCBytes for reuse
        // There are 2 byte arrays in SectionManager
        // 1. holdPKGNAMCBytes which stores the PKGNAMCBytes when holdability is set
        // 2. noHoldPKGNAMCBytes which stores the PKGNAMCBytes when holdability is non hold
        // Note for generated sections, PKGNAMCBytes is generated by the server.
        if (!isGenerated) {
            if (resultSetHoldability_ == ResultSet.HOLD_CURSORS_OVER_COMMIT) {
                PKGNAMCBytes = agent_.sectionManager_.holdPKGNAMCBytes;
            } else if (resultSetHoldability_ == ResultSet.CLOSE_CURSORS_AT_COMMIT) {
                PKGNAMCBytes = agent_.sectionManager_.noHoldPKGNAMCBytes;
            }
        }
    }

    /**
     * Store the Packagename and consistency token information for
     * reuse.
     * <ul>
     *    <li>Case 1: if it is generated section, just store the byte
     *        array in PKGNAMCBytes.</li>
     *    <li>Case 2: for not a generated section, information is
     *        stored in the correct byte array depending on the holdability
     *        in SectionManager.</li>
     * </ul>
     * @arg b the Packagename and consistency token information to store;
     *      should never be null.
     */
    public void setPKGNAMCBytes(byte[] b) {
        if (isGenerated) {
            PKGNAMCBytes = b.clone();
        } else {
            agent_.sectionManager_.setPKGNAMCBytes(b, resultSetHoldability_);
        }
    }

    /**
     * retrieve the package name and consistency token information
     */
    public byte[] getPKGNAMCBytes() {
        return PKGNAMCBytes != null ?
               PKGNAMCBytes.clone() :
               null;
    }

    public String getPackageName() {
        return this.packageName;
    }


    // Add a finalizer to free() the section, useful for Statement.executes that result in exceptions

    public int getSectionNumber() {
        return this.sectionNumber;
    }

    public String getPackage() {
        return this.packageName;
    }

    public String getServerCursorName() {
        return this.serverCursorName;
    }

    // ------------------------ transient members --------------------------------

    public String serverCursorNameForPositionedUpdate_ = null; // member for positioned update sections only
    transient protected String clientCursorName_; // As given by jdbc setCursorName(), this can change

    public String getServerCursorNameForPositionedUpdate() {
        return serverCursorNameForPositionedUpdate_;
    }

    public String getClientCursorName() {
        return clientCursorName_;
    }

    public void setClientCursorName(String clientCursorName) { //
        //System.out.println("clientCursorName is set"+ clientCursorName);
        this.clientCursorName_ = clientCursorName;
    }

    protected Agent agent_;


    public void free() {
        if (resultSetHoldability_ != -1) {
            this.agent_.sectionManager_.freeSection(this, resultSetHoldability_);
        }
    }

    public boolean isReservedPositionedUpdate() {
        return false;
    }

    public int getStaticStatementType() {
        return 0;
    }

    public Section getPositionedUpdateSection() throws SqlException {
        return agent_.sectionManager_.getPositionedUpdateSection(this);
    }

    public void setCursorName(String name) {
        serverCursorName = name;
    }

}

