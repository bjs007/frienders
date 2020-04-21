package com.frienders.main.model;

public class ChildNodeWithDBReference {
    String name;
    String childDbRef;
    String currentNodeDbRef;


    public ChildNodeWithDBReference()
    {

    }
    public ChildNodeWithDBReference(String name
            , String dbRef, String currentNodeDbRef)
    {
        this.name = name;
        this.childDbRef = dbRef;
        this.currentNodeDbRef = currentNodeDbRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChildDbRef() {
        return childDbRef;
    }

    public void setChildDbRef(String childDbRef) {
        this.childDbRef = childDbRef;
    }

    public String getCurrentNodeDbRef() {
        return currentNodeDbRef;
    }

    public void setCurrentNodeDbRef(String currentNodeDbRef) {
        this.currentNodeDbRef = currentNodeDbRef;
    }
}
