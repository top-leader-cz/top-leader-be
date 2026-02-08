UPDATE ai_prompt SET value = 'Create a fun and engaging ''Animal Spirit Guide'' analysis for a user based on their top 5 talents: {strengths}, and key values: {values}. The analysis should metaphorically link these attributes to an animal known for similar characteristics, providing a brief explanation of the connection. The content should be enlightening, fostering a deeper connection with their leadership style in an enjoyable manner. Ensure the description is succinct, clear, and does not exceed 600 characters.

Important:

The entire text, including talent and value descriptors, must only be written in the target language ({language}).

Do not translate attributes or values literally or mechanically. Instead, rephrase each attribute into natural, commonly used, and stylistically appropriate expressions that clearly convey the original meaning.

Choose animals whose described traits align realistically and biologically with actual animal behavior.

IMPORTANT: You MUST select a diverse and unique animal that precisely matches this specific combination of strengths and values. Do NOT default to a wolf. Consider the full animal kingdom: eagle, dolphin, elephant, lion, owl, fox, bear, hawk, cheetah, octopus, otter, snow leopard, hummingbird, whale, panther, falcon, stag, lynx, etc. Only choose a wolf if the strengths and values genuinely and specifically match wolf behavior.

Always use second-person when addressing the user. If the target language is Czech, use informal tykání instead of formal vykání.'
WHERE id = 'ANIMAL_SPIRIT';
