/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.dataflow.sdk.util;

import com.google.cloud.dataflow.sdk.runners.worker.SourceFactory;
import com.google.cloud.dataflow.sdk.util.common.worker.Source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for working with Source Dataflow API definitions and {@link Source}
 * objects.
 */
public class CloudSourceUtils {
  /**
   * Returns a copy of the source with {@code baseSpecs} flattened into {@code spec}.
   * On conflict for a parameter name, values in {@code spec} override values in {@code baseSpecs},
   * and later values in {@code baseSpecs} override earlier ones.
   */
  public static com.google.api.services.dataflow.model.Source
      flattenBaseSpecs(com.google.api.services.dataflow.model.Source source) {
    if (source.getBaseSpecs() == null) {
      return source;
    }
    Map<String, Object> params = new HashMap<>();
    for (Map<String, Object> baseSpec : source.getBaseSpecs()) {
      params.putAll(baseSpec);
    }
    params.putAll(source.getSpec());

    com.google.api.services.dataflow.model.Source result = source.clone();
    result.setSpec(params);
    result.setBaseSpecs(null);
    return result;
  }

  /** Reads all elements from the given {@link Source}. */
  public static <T> List<T> readElemsFromSource(Source<T> source) {
    List<T> elems = new ArrayList<>();
    try (Source.SourceIterator<T> it = source.iterator()) {
      while (it.hasNext()) {
        elems.add(it.next());
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read from source: " + source, e);
    }
    return elems;
  }

  /**
   * Creates a {@link Source} from the given Dataflow Source API definition and
   * reads all elements from it.
   */
  public static <T> List<T> readElemsFromSource(
      com.google.api.services.dataflow.model.Source source) {
    try {
      return readElemsFromSource(SourceFactory.<T>create(null, source, null));
    } catch (Exception e) {
      throw new RuntimeException("Failed to read from source: " + source.toString(), e);
    }
  }
}
