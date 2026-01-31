# MVP
1. Package rename from OMaaP to more generic like example
2. Connection properties for elastic logs commented out.
3. Prompt Improvements with Limit of one day logs and type of logs(e.g. Error ) as Error only.
   Option to limit per applications such as PDA. Add Integration Tests.
4. Prompt to attach the logs to summarize the logs analysis.
5. Review code with ChatGPT/CoPilot for prompts.

# Future Enhancements
1. Store the output of model somewhere like database. Full results in DB.
2. Add Guardrails to Limit logs time range and type of Error only. And Limit on Log file size as well if it is attached.
3. Add Guardrails on prompt to avoid any prompt injection. 
4. Provide Option to switch from Offline to Online Models
5. Display AI Summary on UI 
6. AI Summary for any specific order if logs are attached.
