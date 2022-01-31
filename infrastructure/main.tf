terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 2.94.0"
    }
  }

  required_version = ">= 0.14.9"
}

provider "azurerm" {
  features {}
}

# Resource group
resource "azurerm_resource_group" "resource_group" {
  name     = "edu-public-notes"
  location = "West Europe"
}

# Container Registry
resource "azurerm_container_registry" "container_registry" {
  name                = "eduPublicNotesContainerRegistry"
  resource_group_name = azurerm_resource_group.resource_group.name
  location            = azurerm_resource_group.resource_group.location
  sku                 = "Basic"
  admin_enabled       = true
}

# Postgres Server (including database)
variable "postgres_server_admin_username" {
  description = "Postgres Server administrator username"
  type        = string
}

variable "postgres_server_admin_password" {
  description = "Postgres Server administrator password"
  type        = string
}

resource "azurerm_postgresql_server" "postgres_server" {
  name                = "edu-public-notes-postgresql-server"
  location            = azurerm_resource_group.resource_group.location
  resource_group_name = azurerm_resource_group.resource_group.name

  sku_name = "B_Gen5_1"

  storage_mb                   = 5120
  backup_retention_days        = 7
  geo_redundant_backup_enabled = false
  auto_grow_enabled            = true

  administrator_login          = var.postgres_server_admin_username
  administrator_login_password = var.postgres_server_admin_password
  version                      = "11"
  ssl_enforcement_enabled      = false
}

resource "azurerm_postgresql_firewall_rule" "postgres_firewall_rule" {
  name                = "edu-public-notes-postgres-firewall-rule"
  resource_group_name = azurerm_resource_group.resource_group.name
  server_name         = azurerm_postgresql_server.postgres_server.name
  start_ip_address    = "0.0.0.0"
  end_ip_address      = "0.0.0.0"
}

# App Service
resource "azurerm_app_service_plan" "app_service_plan" {
  name                = "edu-public-notes-app-service-plan"
  location            = azurerm_resource_group.resource_group.location
  resource_group_name = azurerm_resource_group.resource_group.name
  kind                = "Linux"
  reserved            = true

  sku {
    tier = "Basic"
    size = "B1"
  }
}

resource "azurerm_app_service" "app_service" {
  name                = "edu-public-notes-service"
  location            = azurerm_resource_group.resource_group.location
  resource_group_name = azurerm_resource_group.resource_group.name
  app_service_plan_id = azurerm_app_service_plan.app_service_plan.id

  site_config {
    linux_fx_version = "DOCKER|edupublicnotescontainerregistry.azurecr.io/service-notes:latest"
  }

  app_settings = {
    "SPRING_DATASOURCE_URL" = "jdbc:postgresql://${azurerm_postgresql_server.postgres_server.fqdn}:5432/postgres"
    "SPRING_DATASOURCE_USER" = "${var.postgres_server_admin_username}@${azurerm_postgresql_server.postgres_server.name}"
    "SPRING_DATASOURCE_PASSWORD" = var.postgres_server_admin_password
    "DOCKER_REGISTRY_SERVER_USERNAME" = azurerm_container_registry.container_registry.admin_username
    "DOCKER_REGISTRY_SERVER_PASSWORD" = azurerm_container_registry.container_registry.admin_password
  }
}
