package com.finsightanalytics.recommendationengine.model;

import org.springframework.data.neo4j.core.schema.Node;

@Node("PeerGroup")
public class Neo4jPeerGroup {
    private String ifscCode; // Bank IFSC code representing the group

    public Neo4jPeerGroup(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    // Getters and setters
    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
}
