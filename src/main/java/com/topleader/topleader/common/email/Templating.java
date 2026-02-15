package com.topleader.topleader.common.email;

import java.util.Map;

public interface Templating {

    String getMessage(Map<String, Object> parameters, String templatePath);
}
