package com.topleader.topleader.ai;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class AiUtils {

    public String replaceNonJsonString(String json) {
        return json.replace("```json", StringUtils.EMPTY).replace("```", StringUtils.EMPTY);
    }

}
