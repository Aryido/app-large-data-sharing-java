/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecodesamples.cloud.jss.lds.controller;

import com.googlecodesamples.cloud.jss.lds.model.Dto;
import com.googlecodesamples.cloud.jss.lds.model.ListDto;
import com.googlecodesamples.cloud.jss.lds.model.Vo;
import com.googlecodesamples.cloud.jss.lds.service.IFileService;
import com.googlecodesamples.cloud.jss.lds.service.OpenTelemetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** REST API controller of the backend service */
@Slf4j
@RestController
@RequestMapping("/api")
public class Controller {
  private static final String STRING_SEPARATOR = "\\s+";
  private final IFileService<Vo> fileService;
  private final OpenTelemetryService openTelemetryService;

  public Controller(IFileService<Vo> fileService, OpenTelemetryService openTelemetryService) {
    this.fileService = fileService;
    this.openTelemetryService = openTelemetryService;
  }

  /**
   * The health check API.
   *
   * @return status OK is the service is alive
   */
  @GetMapping("/healthchecker")
  public ResponseEntity<?> healthCheck() throws Exception {
    return openTelemetryService.spanScope(this.getClass().getName(), "healthCheck", () -> {
      log.info("entering healthCheck()");
      return ResponseEntity.noContent().build();
    });
  }

  /**
   * Upload files with tags.
   *
   * @param files list of files upload to the server
   * @param tags list of tags (separated by space) label the files
   * @return list of uploaded files
   */
  @PostMapping("/files")
  public ResponseEntity<?> uploadFiles(
      @RequestParam List<MultipartFile> files, @RequestParam String tags) throws Exception {
    return openTelemetryService.spanScope(this.getClass().getName(), "uploadFiles", () -> {
      log.info("entering uploadFiles()");
      List<String> tagList = getTagList(tags);
      List<Vo> fileList = fileService.createFiles(files, tagList);

      return ResponseEntity.status(HttpStatus.CREATED).body(ListDto.<Vo>builder()
              .files(fileList)
              .build());
    });
  }

  /**
   * Search files with the given tags.
   *
   * @param tags list of tags (separated by space) label the files
   * @param orderNo order number of the last file
   * @param size number of files return
   * @return list of files with pagination
   */
  @GetMapping("/files")
  public ResponseEntity<?> getFilesByTag(
      @RequestParam(required = false) String tags,
      @RequestParam(required = false) String orderNo,
      @RequestParam(required = false, defaultValue = "50") int size) throws Exception {
    return openTelemetryService.spanScope(this.getClass().getName(), "getFilesByTag", () -> {
      log.info("entering getFilesByTag()");
      List<String> tagList = getTagList(tags);
      List<Vo> fileList = fileService.findFilesBy(tagList, orderNo, size);
      if (CollectionUtils.isEmpty(fileList)) {
        return ResponseEntity.ok().body(ListDto.<Vo>builder().files(new ArrayList<>()).build());
      }
      return ResponseEntity.ok().body(ListDto.<Vo>builder()
              .files(fileList)
              .build());
    });
  }

  /**
   * Update an existing file
   *
   * @param fileId unique ID of the file
   * @param file new file to be uploaded to the server
   * @param tags list of tags (separated by space) label the new file
   * @return file data
   */
  @PutMapping("/files/{id}")
  public ResponseEntity<?> updateFile(
      @PathVariable("id") String fileId,
      @RequestParam(required = false) MultipartFile file,
      @RequestParam String tags) throws Exception {
    return openTelemetryService.spanScope(this.getClass().getName(), "updateFile", () -> {
      log.info("entering updateFile()");
      Optional<Vo> optionalVo = fileService.findFileById(fileId);
      if (optionalVo.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      List<String> tagList = getTagList(tags);
      Vo vo = fileService.updateFile(file, tagList, optionalVo.get());
      return ResponseEntity.ok().body(Dto.<Vo>builder().file(vo).build());
    });
  }

  /**
   * Delete an existing file
   *
   * @param fileId unique ID of the file
   * @return status NoContent or NotFound
   */
  @DeleteMapping("/files/{id}")
  public ResponseEntity<?> deleteFile(@PathVariable("id") String fileId) throws Exception {
    return openTelemetryService.spanScope(this.getClass().getName(), "deleteFile", () -> {
      log.info("entering deleteFile()");
      Optional<Vo> optionalVo = fileService.findFileById(fileId);
      if (optionalVo.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      fileService.deleteFile(optionalVo.get());
      return ResponseEntity.noContent().build();
    });
  }

  /**
   * Delete all files.
   *
   * @return status NoContent
   */
  @DeleteMapping("/reset")
  public ResponseEntity<?> resetFile() throws Exception {
    return openTelemetryService.spanScope(this.getClass().getName(), "resetFile", () -> {
      log.info("entering resetFile()");
      fileService.resetFile();
      return ResponseEntity.noContent().build();
    });
  }

  /**
   * Split the string by separator.
   *
   * @param tags list of tags in a single string (separated by space)
   * @return list of tags
   */
  private List<String> getTagList(String tags) {
    if (!StringUtils.hasText(tags)) {
      return new ArrayList<>();
    }
    return Arrays.stream(tags.split(STRING_SEPARATOR))
        .map(String::trim)
        .map(String::toLowerCase)
        .collect(Collectors.toList());
  }
}
