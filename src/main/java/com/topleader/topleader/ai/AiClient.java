package com.topleader.topleader.ai;


import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class AiClient {

    public static final String LEADERSHIP_STYLE_QUERY = "Given a user's top 5 strengths: %s, and key values: %s, provide a comprehensive yet concise leadership style analysis. Highlight how their unique strengths and values combine to shape their approach to leadership. The analysis should be straightforward and resonate with users of varying backgrounds. Aim for an output that is inspiring and provides clear direction on how they can apply their strengths and values in their leadership role. Keep the analysis under 1000 characters. The text is to be in %s language. Use second person when addressing the user.";

    public static final String ANIMAL_SPIRIT_QUERY = "Create a fun and engaging 'Animal Spirit Guide' analysis for a user based on their top 5 strengths: %s, and key values: %s. The analysis should metaphorically link these attributes to an animal known for similar characteristics, providing a brief explanation of the connection. The content should be enlightening, fostering a deeper connection with their leadership style in an enjoyable manner. Ensure the description is succinct, clear, and does not exceed 600 characters. The text is to be in %s language. Use second person when addressing the user.";

    public static final String LEADERSHIP_TIP_QUERY = "Generate a daily leadership tip within 500 characters, tailored to a user whose top 5 strengths are: %s, and whose key values include: %s. The tip should provide actionable advice, encouraging the user to apply their strengths and values in daily leadership scenarios. Make it inspiring yet practical, suited for leaders looking to improve their skills and positively influence their team. The text is to be in %s language. Use second person when addressing the user.";

    public static final String PERSONAL_GROWTH_TIP_QUERY = "Create a 'Personal Growth Tip' within 500 characters, tailored to a user whose top strengths are %s, and key values include %s. The fact should be related to these strengths and values, offering unique and interesting insights into how they can be leveraged in leadership and self-improvement. Aim for the content to be specific, engaging, and directly relevant to the user's personal development profile. The text is to be in %s language. Use second person when addressing the user.";

    private final ChatClient chatClient;

    public String findLeaderShipStyle(String locale, List<String> strengths, List<String> values) {
        return chatClient.call(String.format(LEADERSHIP_STYLE_QUERY, strengths, values, locale));

    }

    public String findAnimalSpirit(String locale, List<String> strengths, List<String> values) {
        return chatClient.call(String.format(ANIMAL_SPIRIT_QUERY, strengths, values, locale));
    }

    public String findLeadershipTip(String locale, List<String> strengths, List<String> values) {
        return chatClient.call(String.format(LEADERSHIP_TIP_QUERY, strengths, values, locale));
    }

    public String findPersonalGrowthTip(String locale, List<String> strengths, List<String> values) {
        return chatClient.call(String.format(PERSONAL_GROWTH_TIP_QUERY, strengths, values, locale));
    }
}
