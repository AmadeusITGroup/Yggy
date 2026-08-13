(function () {
  "use strict";

  var MAX_RESPONSE_BYTES = 2 * 1024 * 1024; // 2MB

  function extractPayload(response) {
    if (typeof response.data === "string") {
      return response.data;
    }

    if (response.data != null) {
      try {
        return JSON.stringify(response.data, null, 2);
      } catch (e) {
        return String(response.data);
      }
    }

    if (typeof response.text === "string") {
      return response.text;
    }

    if (typeof response.body === "string") {
      return response.body;
    }

    return "";
  }

  function getHeader(response, key) {
    var headers = response && response.headers ? response.headers : {};
    return headers[key] || headers[key.toLowerCase()] || headers[key.toUpperCase()] || "";
  }

  function parseFilename(contentDisposition) {
    if (!contentDisposition) {
      return null;
    }

    var utf8 = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
    if (utf8 && utf8[1]) {
      try {
        return decodeURIComponent(utf8[1]);
      } catch (e) {
        return utf8[1];
      }
    }

    var basic = contentDisposition.match(/filename="?([^";]+)"?/i);
    return basic && basic[1] ? basic[1] : null;
  }

  function extensionFromContentType(contentType) {
    var ct = (contentType || "").toLowerCase();
    if (ct.indexOf("json") >= 0) {
      return "json";
    }
    if (ct.indexOf("csv") >= 0) {
      return "csv";
    }
    if (ct.indexOf("text/plain") >= 0) {
      return "txt";
    }
    return "bin";
  }

  function byteLength(text) {
    return new TextEncoder().encode(text).length;
  }

  function download(content, contentType, filename) {
    var blob = new Blob([content], { type: contentType || "application/octet-stream" });
    var objectUrl = URL.createObjectURL(blob);
    var a = document.createElement("a");
    a.href = objectUrl;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(objectUrl);
  }

  // Referenced by quarkus.swagger-ui.response-interceptor in application.properties.
  window.downloadLargeResponseInterceptor = function (response) {
    try {
      var payload = extractPayload(response);
      var size = byteLength(payload);

      if (size <= MAX_RESPONSE_BYTES) {
        return response;
      }

      var contentType = getHeader(response, "content-type") || "application/json";
      var contentDisposition = getHeader(response, "content-disposition");
      var filename = parseFilename(contentDisposition) || ("api_swagger." + extensionFromContentType(contentType));

      download(payload, contentType, filename);

      var message =
        "Response too large for inline display (" +
        size +
        " bytes). Download triggered: " +
        filename;

      // Ensure Swagger UI does not attempt to render the original large payload.
      response.data = message;
      response.text = message;
      response.body = message;

      if (!response.headers) {
        response.headers = {};
      }
      response.headers["content-type"] = "text/plain; charset=utf-8";
      response.headers["Content-Type"] = "text/plain; charset=utf-8";
    } catch (e) {
      // Keep default Swagger behavior on any interception issue.
    }

    return response;
  };
})();
