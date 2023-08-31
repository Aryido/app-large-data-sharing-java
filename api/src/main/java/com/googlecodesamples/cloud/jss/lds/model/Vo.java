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

package com.googlecodesamples.cloud.jss.lds.model;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * The BaseFile class represents a file being uploaded by the users
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vo extends Dao {
  public static final List<String> IMG_EXTENSIONS = List.of("png", "jpeg", "jpg", "gif");
  public static final String THUMBNAIL_EXTENSION = "_small";

  private String url;
  private String thumbUrl;
  private Date createTime;
  private Date updateTime;
}
