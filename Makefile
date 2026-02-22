SHELL := /bin/bash
COMPOSE := docker compose
ENV_FILE ?= .env

.PHONY: help init-env check-env build up deploy down restart logs ps clean app-shell db-shell

help:
	@echo "Targets:"
	@echo "  make init-env   # create .env from .env.example"
	@echo "  make build      # build app image"
	@echo "  make up         # start mysql + app in background"
	@echo "  make deploy     # alias of make up"
	@echo "  make down       # stop services"
	@echo "  make restart    # restart services"
	@echo "  make logs       # tail app/mysql logs"
	@echo "  make ps         # show container status"
	@echo "  make clean      # stop and remove volumes (DANGEROUS: wipes db data)"
	@echo "  make app-shell  # shell into app container"
	@echo "  make db-shell   # mysql shell using app user"

init-env:
	@if [ -f $(ENV_FILE) ]; then \
		echo "$(ENV_FILE) already exists"; \
	else \
		cp .env.example $(ENV_FILE); \
		echo "Created $(ENV_FILE), please edit secrets first"; \
	fi

check-env:
	@if [ ! -f $(ENV_FILE) ]; then \
		echo "Missing $(ENV_FILE). Run: make init-env"; \
		exit 1; \
	fi

build: check-env
	$(COMPOSE) --env-file $(ENV_FILE) build --pull

up: check-env
	$(COMPOSE) --env-file $(ENV_FILE) up -d --build

deploy: up

down:
	$(COMPOSE) --env-file $(ENV_FILE) down

restart: down up

logs:
	$(COMPOSE) --env-file $(ENV_FILE) logs -f app mysql

ps:
	$(COMPOSE) --env-file $(ENV_FILE) ps

clean:
	$(COMPOSE) --env-file $(ENV_FILE) down -v --remove-orphans

app-shell:
	$(COMPOSE) --env-file $(ENV_FILE) exec app sh

db-shell:
	$(COMPOSE) --env-file $(ENV_FILE) exec mysql mysql -u$$MYSQL_USER -p$$MYSQL_PASSWORD $$MYSQL_DATABASE
