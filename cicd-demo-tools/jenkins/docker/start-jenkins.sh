#!/bin/bash
#  START JENKINS AGENT USING JNLP
#
#  REQUIRED ENVIRONMENT VARS:
#  - JENKINS_JNLP_URL      - URL OF CONTROLLER JNLP URL FOR SPECIFIC AGENT
#  - JENKINS_AGENT_SECRET  - OBTAINED FROM CONTROLLER WHEN DEFINING AGENT
#  - WORK_DIR
#
java -jar /usr/share/jenkins/agent.jar -jnlpUrl $JENKINS_JNLP_URL -secret $JENKINS_AGENT_SECRET -workDir $WORK_DIR
