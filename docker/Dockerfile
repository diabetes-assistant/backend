FROM adoptopenjdk:16-jre
ENV USER_HOME=/opt/app
RUN mkdir ${USER_HOME}
COPY . ${USER_HOME}
RUN useradd -ms /bin/bash -d ${USER_HOME} diabetes-assistant-backend
RUN chown -R diabetes-assistant-backend: ${USER_HOME}
WORKDIR ${USER_HOME}
CMD ["java", "-jar", "/opt/app/app.jar"]
