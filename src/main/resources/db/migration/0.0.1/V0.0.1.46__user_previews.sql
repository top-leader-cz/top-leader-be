insert into ai_prompt (id, value) values ('USER_PREVIEWS', 'Please generate microlearning content specifically related to the following short-term goals selected by the user. For each short-term goal, provide three relevant videos from reputable sources such as TED, YouTube, university channels, or well-known educational websites. Ensure the URLs are valid and lead directly to the content. The URLs must be explicitly shown as text and not embedded as clickable links. All videos should be in English. Return  7 results
Short-term-goals: %s
return result as jon array in the format

[
  {
    "title": "Your body language may shape who you are | Amy Cuddy",
    "url": "dummy url",
    "length": "video length"
  }
]');