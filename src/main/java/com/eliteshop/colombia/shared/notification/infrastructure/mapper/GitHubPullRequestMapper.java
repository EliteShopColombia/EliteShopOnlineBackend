package com.eliteshop.colombia.shared.notification.infrastructure.mapper;

import com.eliteshop.colombia.shared.notification.infrastructure.controller.dto.GitHubWebhookPayload;

public class GitHubPullRequestMapper {

  private GitHubPullRequestMapper() {}

  public static String toSlackMessage(GitHubWebhookPayload payload) {
    String icon;
    String action;

    switch (payload.getAction()) {
      case "opened" -> {
        icon = ":large_blue_circle:";
        action = "abrió";
      }
      case "closed" -> {
        icon = ":white_check_mark:";
        action = "cerró";
      }
      case "reopened" -> {
        icon = ":recycle:";
        action = "reabrió";
      }
      case "synchronize" -> {
        icon = ":pencil2:";
        action = "actualizó";
      }
      default -> {
        icon = ":pushpin:";
        action = payload.getAction();
      }
    }

    GitHubWebhookPayload.PullRequest pr = payload.getPullRequest();

    return String.format(
        "%s *PR #%d: %s*\nAutor: %s\nBranch: `%s` → `%s`\nAcción: %s\n:link: %s",
        icon,
        pr.getNumber(),
        pr.getTitle(),
        pr.getUser().getLogin(),
        pr.getHead().getRef(),
        pr.getBase().getRef(),
        action,
        pr.getHtmlUrl());
  }
}
