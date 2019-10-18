package com.hashtagmentions.text;


public class TagBean {

  private String name;
  private String id;

  public TagBean(String name, String id) {
    this.name = name;
    this.id = id;
  }

  @Override public String toString() {
    return "{" +
        "name='" + name + '\'' +
        ", id='" + id + '\'' +
        '}';
  }
}
