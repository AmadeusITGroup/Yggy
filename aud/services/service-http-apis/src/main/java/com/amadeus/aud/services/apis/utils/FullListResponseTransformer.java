package com.amadeus.aud.services.apis.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FullListResponseTransformer {
  private static final String INSTANCE = "instance";
  private static final String INSTANCE_ = "_instance";
  private static final String CIDRS = "cidrs";

  /** Separator used in composite BOM keys: {@code "Provider***Name"}. */
  private static final String COMPOSITE_KEY_SEPARATOR = "***";

  private static final int COMPOSITE_KEY_SEPARATOR_LENGTH = COMPOSITE_KEY_SEPARATOR.length();

  private static final Map<String, String> REF_FIELD_TO_TYPE =
      Map.ofEntries(
          Map.entry("landing_zones", "landingzone"),
          Map.entry("regions", "region"),
          Map.entry("targets", "target"),
          Map.entry("application_phases", "applicationphase"),
          Map.entry("connection_phases", "connectionphase"),
          Map.entry("firewall_groups", "firewallgroup"),
          Map.entry("zone_instances", "zoneinstance"),
          Map.entry("subnet_instances", "subnetinstance"),
          Map.entry("secured_scopes", "scope"),
          Map.entry("authorized_scopes", "scope"),
          Map.entry("components", "component"),
          Map.entry("parents", "component"),
          Map.entry("automatic_template_instances", "automatictemplateinstance"),
          Map.entry("predecessors", "automationtype"),
          Map.entry("target_services", "service"),
          Map.entry("hosted_component_instances", "componentinstance"));

  /**
   * Maps LOCAL field names (snake_case from mixins) to Python field names. Derived from the inverse
   * of the {@code propertyTranslator} map in AbstractBaseResourceIT, plus the ref-array key renames
   * derived from the remove-underscores convention.
   */
  private static final Map<String, String> FIELD_RENAME_MAP =
      Map.ofEntries(
          Map.entry("macro_phase", "macrophase"),
          Map.entry("is_customer_facing", "is_customerfacing"),
          Map.entry("landing_zone", "landingzone"),
          Map.entry("zone_type", "zonetype"),
          Map.entry("service_tag", "servicetag"),
          Map.entry("service_now_id", "servicenowid"),
          Map.entry("security_area", "securityarea"),
          Map.entry("recertification_service_now_id", "recertification_servicenowid"),
          Map.entry("ad_group", "adgroup"),
          Map.entry("target_type", "targettype"),
          Map.entry("enable_erding_jump_server_access", "enable_erding_jumpserver_access"),
          Map.entry("is_aud_modelized", "is_audmodelized"),
          Map.entry("macro_phase_order", "macrophase_order"),
          Map.entry("firewall_vendor", "firewallvendor"),
          Map.entry("firewall_group", "firewallgroup"),
          Map.entry("is_network_private", "is_networkprivate"),
          Map.entry("subnet_type", "subnettype"),
          Map.entry("landing_zone_type", "landingzonetype"),
          Map.entry("skip_source", "skipsource"),
          Map.entry("skip_destination", "skipdestination"),
          Map.entry("firewall_level", "firewalllevel"),
          Map.entry("security_control", "securitycontrol"),
          Map.entry("connection_type", "connectiontype"),
          Map.entry("source_zone_type", "source_zonetype"),
          Map.entry("destination_zone_type", "destination_zonetype"),
          Map.entry("source_macro_phase", "source_macrophase"),
          Map.entry("destination_macro_phase", "destination_macrophase"),
          Map.entry("is_db_synchro", "is_dbsynchro"),
          Map.entry("application_id", "applicationid"),
          Map.entry("is_source_limited_to_landing_zone", "is_source_limited_to_landingzone"),
          Map.entry("is_source_limited_to_macro_phase", "is_source_limited_to_macrophase"),
          Map.entry(
              "is_target_limited_to_source_security_zone",
              "is_target_limited_to_source_securityzone"),
          Map.entry(
              "is_target_limited_to_source_application_phase",
              "is_target_limited_to_source_applicationphase"),
          Map.entry(
              "is_target_limited_to_source_landing_zone",
              "is_target_limited_to_source_landingzone"),
          Map.entry(
              "is_target_limited_to_source_macro_phase", "is_target_limited_to_source_macrophase"),
          Map.entry("application_phases", "applicationphases"),
          Map.entry("landing_zones", "landingzones"),
          Map.entry("firewall_groups", "firewallgroups"),
          Map.entry("automatictemplateinstance", "automatictemplate_instance"),
          Map.entry("automatic_template_instances", "automatictemplate_instances"),
          Map.entry("alias", "p_account"),
          Map.entry("serviceNowId", "servicenowid"));

  private FullListResponseTransformer() {}

  public static byte[] transform(ObjectMapper mapper, byte[] raw, String objectType)
      throws IOException {
    JsonNode root = mapper.readTree(raw);
    if (!(root instanceof ObjectNode responseNode)) {
      return raw;
    }

    // 1. Rename "values" → object-type key (matching Python convention: lowercase, only _instance
    // suffix)
    String typeKey = objectType.toLowerCase(Locale.ROOT).replace(INSTANCE, INSTANCE_);
    JsonNode valuesNode = responseNode.remove("values");
    if (valuesNode != null && valuesNode.isArray()) {
      ArrayNode items = (ArrayNode) valuesNode;
      // 2. Transform each item's ref-array fields
      for (JsonNode item : items) {
        if (item instanceof ObjectNode obj) {
          transformItem(mapper, obj);
        }
      }
      responseNode.set(typeKey, items);
    }

    return mapper.writeValueAsBytes(responseNode);
  }

  /**
   * For each field in an item:
   *
   * <ul>
   *   <li>{@code tags}: if it is an ObjectNode {@code {key: value, ...}}, convert it to the Python
   *       format {@code [{"key":"...","value":"..."},...]}
   *   <li>Known reference fields: rename key via {@link #FIELD_RENAME_MAP}; if elements are still
   *       plain strings, convert them to {@code {"type":"<ref_type>","name":"<name>"}} where the
   *       name is extracted from the composite key {@code "Provider***Name"} (part after {@code
   *       ***})
   *   <li>All other fields present in {@link #FIELD_RENAME_MAP}: rename key to Python convention
   * </ul>
   */
  private static void transformItem(ObjectMapper mapper, ObjectNode item) {
    fixTypeField(item);

    // Collect field names first to avoid ConcurrentModificationException
    List<String> fieldNames = new ArrayList<>();
    item.fieldNames().forEachRemaining(fieldNames::add);

    for (String fieldName : fieldNames) {
      transformField(mapper, item, fieldName);
    }
  }

  /** Normalises the {@code type} field value: {@code "...instance"} → {@code "..._instance"}. */
  private static void fixTypeField(ObjectNode item) {
    JsonNode typeNode = item.get("type");
    if (typeNode != null && typeNode.isTextual()) {
      String t = typeNode.asText();
      if (t.endsWith(INSTANCE) && !t.endsWith(INSTANCE_)) {
        item.put("type", t.replace(INSTANCE, INSTANCE_));
      }
    }
  }

  /** Dispatches a single field to the appropriate transformation. */
  private static void transformField(ObjectMapper mapper, ObjectNode item, String fieldName) {
    if (CIDRS.equals(fieldName)) {
      transformCidrsField(item);
      return;
    }
    if ("tags".equals(fieldName)) {
      transformTagsField(mapper, item);
      return;
    }
    String refType = REF_FIELD_TO_TYPE.get(fieldName);
    if (refType != null) {
      transformRefArrayField(mapper, item, fieldName, refType);
      return;
    }
    String pythonName = FIELD_RENAME_MAP.get(fieldName);
    if (pythonName != null) {
      renameField(item, fieldName, pythonName);
      return;
    }
    transformGenericArrayField(mapper, item, fieldName);
  }

  /** Converts the {@code cidrs} array to a comma-separated string (Python format). */
  private static void transformCidrsField(ObjectNode item) {
    JsonNode cidrsNode = item.get(CIDRS);
    if (!(cidrsNode instanceof ArrayNode cidrsArr)) {
      return;
    }
    List<String> cidrList = new ArrayList<>();
    for (JsonNode el : cidrsArr) {
      cidrList.add(el.asText());
    }
    item.put(CIDRS, String.join(",", cidrList));
  }

  /** Converts the {@code tags} object {@code {key: value}} to {@code [{key, value}]} array. */
  private static void transformTagsField(ObjectMapper mapper, ObjectNode item) {
    JsonNode tagsNode = item.get("tags");
    if (!(tagsNode instanceof ObjectNode tagsObj)) {
      return;
    }
    ArrayNode tagsArray = mapper.createArrayNode();
    tagsObj
        .properties()
        .forEach(
            (Map.Entry<String, JsonNode> e) -> {
              ObjectNode tagEntry = mapper.createObjectNode();
              tagEntry.put("key", e.getKey());
              tagEntry.set("value", e.getValue());
              tagsArray.add(tagEntry);
            });
    item.set("tags", tagsArray);
  }

  /**
   * Handles a known reference-array field: converts plain-string elements to {@code {type, name}}
   * objects (stripping the {@code Provider***} composite-key prefix), or recurses into existing
   * object elements. Then renames the field key via {@link #FIELD_RENAME_MAP}.
   */
  private static void transformRefArrayField(
      ObjectMapper mapper, ObjectNode item, String fieldName, String refType) {
    JsonNode fieldValue = item.get(fieldName);
    if (!(fieldValue instanceof ArrayNode original)) {
      return;
    }
    ArrayNode result = buildRefArray(mapper, original, refType);
    String pythonFieldName = FIELD_RENAME_MAP.getOrDefault(fieldName, fieldName);
    item.remove(fieldName);
    item.set(pythonFieldName, result);
  }

  /**
   * Builds the transformed ref array: either converts string elements to {@code {type, name}}
   * objects, or recurses into existing object elements.
   */
  private static ArrayNode buildRefArray(ObjectMapper mapper, ArrayNode original, String refType) {
    boolean allStrings = true;
    for (JsonNode el : original) {
      if (!el.isTextual()) {
        allStrings = false;
        break;
      }
    }
    if (allStrings) {
      return convertStringsToRefObjects(mapper, original, refType);
    }
    for (JsonNode el : original) {
      if (el instanceof ObjectNode nestedObj) {
        transformItem(mapper, nestedObj);
      }
    }
    return original;
  }

  /** Converts each string element to a {@code {"type": refType, "name": name}} object node. */
  private static ArrayNode convertStringsToRefObjects(
      ObjectMapper mapper, ArrayNode original, String refType) {
    ArrayNode result = mapper.createArrayNode();
    for (JsonNode el : original) {
      ObjectNode refObj = mapper.createObjectNode();
      refObj.put("type", refType);
      // Strip composite-key prefix "Provider***Name" → keep only the name part
      String raw = el.asText();
      int sep = raw.indexOf(COMPOSITE_KEY_SEPARATOR);
      refObj.put("name", sep >= 0 ? raw.substring(sep + COMPOSITE_KEY_SEPARATOR_LENGTH) : raw);
      result.add(refObj);
    }
    return result;
  }

  /** Renames a field key in-place. */
  private static void renameField(ObjectNode item, String oldName, String newName) {
    JsonNode value = item.remove(oldName);
    item.set(newName, value);
  }

  /** Recurses into nested object elements of a generic array field (e.g. {@code members}). */
  private static void transformGenericArrayField(
      ObjectMapper mapper, ObjectNode item, String fieldName) {
    JsonNode val = item.get(fieldName);
    if (!(val instanceof ArrayNode genericArr)) {
      return;
    }
    for (JsonNode el : genericArr) {
      if (el instanceof ObjectNode nestedObj) {
        transformItem(mapper, nestedObj);
      }
    }
  }
}
