package com.keuin.crosslink.util.version;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class NewVersionChecker {

    public static void checkNewVersionAsync(Consumer<String> linePrinter) {
        checkNewVersionAsync(linePrinter, true);
    }

    public static void checkNewVersionAsync(Consumer<String> linePrinter, boolean skipPreRelease) {
        // do not block the main routine, so check in another thread
        new Thread(() -> {
            try {
                if (VersionInfo.DIRTY != 0) {
                    // dirty build is never released, so is not comparable
                    linePrinter.accept("You are running a test build of CrossLink, which is unstable. " +
                            "Update checking is disabled. Please switch to official release if possible.");
                }
                final var req = new Request.Builder()
                        .header("Accept", "application/vnd.github.v3+json")
                        .url("https://api.github.com/repos/keuin/crosslink/releases")
                        .build();
                final var client = new OkHttpClient();
                final var mainResp = client.newCall(req).execute();
                final var mapper = new ObjectMapper();
                final var json = mapper.readTree(Objects.requireNonNull(mainResp.body()).byteStream());
                for (var ver : json) {
                    if (ver == null) continue;
                    final var commit = ver.get("target_commitish").textValue();
                    if (VersionInfo.GIT_SHA.equalsIgnoreCase(commit)) {
                        linePrinter.accept("Current version is the latest version.");
                        return; // current version is the latest version
                    }
                    if (ver.get("draft").booleanValue()) continue;
                    if (ver.get("prerelease").booleanValue() && skipPreRelease) continue;

                    // this is a new version, notify the user
                    final var pageUrl = ver.get("html_url").textValue();
                    final var tag = ver.get("tag_name").textValue();
                    final var name = ver.get("name").textValue();
                    final var publishDate = Instant
                            .parse(ver.get("published_at").textValue())
                            .atZone(ZoneId.of("UTC")).toLocalDateTime();
                    final var currentVersionDate = Instant
                            .parse(VersionInfo.BUILD_DATE).atZone(ZoneId.of("UTC")).toLocalDateTime();
                    final var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm.ss");

                    // get detail message here
                    var detailMessage = "";
                    try {
                        var detailResp = client.newCall(new Request.Builder()
                                .header("Accept", "application/vnd.github.v3+json")
                                .url(ver.get("url").textValue()).build()).execute();
                        var detail = mapper.readTree(Objects.requireNonNull(detailResp.body()).byteStream());
                        detailMessage = Optional.ofNullable(detail.get("body").textValue()).orElse("");
                    } catch (Exception ignored) {
                    }

                    linePrinter.accept("=".repeat(32));
                    linePrinter.accept("New version of CrossLink is available!");
                    linePrinter.accept("");
                    linePrinter.accept(String.format("Current Version: %s, Build Time: %s",
                            VersionInfo.VERSION, currentVersionDate.format(fmt)));
                    linePrinter.accept(String.format("New Version: %s, Description: %s", tag, name));
                    linePrinter.accept(String.format("Release Date: %s", publishDate.format(fmt)));
                    linePrinter.accept(String.format("Git Commit: %s", commit));
                    linePrinter.accept(String.format("URL: %s", pageUrl));
                    if (!detailMessage.isEmpty()) {
                        linePrinter.accept("Updates:");
                        for (String s : detailMessage.split("\n")) {
                            linePrinter.accept(s);
                        }
                    }
                    linePrinter.accept("If you want to disable update checker, " +
                            "edit \"general.json\" and set \"check_update\" to false.");
                    linePrinter.accept("=".repeat(32));
                    return;
                }
            } catch (Exception ex) {
                linePrinter.accept("Cannot check new version from GitHub.");
//                throw new RuntimeException(ex);
            }
        }).start();
    }
}
