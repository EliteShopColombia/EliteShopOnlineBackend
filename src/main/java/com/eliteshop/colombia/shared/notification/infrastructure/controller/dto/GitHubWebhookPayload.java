package com.eliteshop.colombia.shared.notification.infrastructure.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubWebhookPayload {

  private String action;

  @JsonProperty("pull_request")
  private PullRequest pullRequest;

  private Repository repository;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PullRequest {

    private int number;
    private String title;

    @JsonProperty("html_url")
    private String htmlUrl;

    private User user;
    private String state;
    private Head head;
    private Base base;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class User {
    private String login;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Head {
    private String ref;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Base {
    private String ref;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Repository {

    @JsonProperty("full_name")
    private String fullName;
  }
}
