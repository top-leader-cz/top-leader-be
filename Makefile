# Google Cloud commands
.PHONY: login logs-qa logs-prod

# Login to Google Cloud and set project
login:
	gcloud auth login topleaderplatform@gmail.com
	gcloud config set project topleader-394306

# Fetch QA logs (last 1 hour, errors only)
logs-qa:
	gcloud logging read 'resource.type="gae_app" AND resource.labels.module_id="qa" AND severity>=ERROR' --limit=50 --format="table(timestamp,severity,textPayload)"

# Fetch QA logs with AI/OpenAI filter
logs-qa-ai:
	gcloud logging read 'resource.type="gae_app" AND resource.labels.module_id="qa" AND (textPayload=~"openai|OpenAI|ChatModel|AiClient" OR textPayload=~"Exception|Error")' --limit=100 --format="json"

# Fetch prod logs
logs-prod:
	gcloud logging read 'resource.type="gae_app" AND resource.labels.module_id="default" AND severity>=ERROR' --limit=50 --format="table(timestamp,severity,textPayload)"
