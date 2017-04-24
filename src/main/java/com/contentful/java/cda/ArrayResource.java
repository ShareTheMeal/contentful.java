package com.contentful.java.cda;

import java.util.List;
import java.util.Map;

/**
 * An abstraction of CDAResources combined into one array.
 *
 * @see CDAResource
 */
abstract class ArrayResource extends CDAResource {
  List<CDAResource> items;

  Map<String, CDAAsset> assets;

  Map<String, CDAEntry> entries;

  /**
   * @return items in this resource.
   */
  public List<CDAResource> items() {
    return items;
  }

  /**
   * @return assets mapped by asset id (includes linked resources).
   */
  public Map<String, CDAAsset> assets() {
    return assets;
  }

  /**
   * @return entries mapped by entry id (includes linked resources).
   */
  public Map<String, CDAEntry> entries() {
    return entries;
  }
}
