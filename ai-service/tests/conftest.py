import os

# Pas d'Eureka pendant les tests (comme eureka.client.enabled=false cote Spring)
os.environ["EUREKA_ENABLED"] = "false"
# Pas d'appel reel a Gemini : les tests qui en ont besoin le simulent
os.environ.pop("GEMINI_API_KEY", None)
