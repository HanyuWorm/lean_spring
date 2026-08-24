terraform {
  required_version = ">= 1.8.0"
}

variable "environment" {
  type        = string
  description = "Tên environment dùng cho lab local."
  default     = "learning"

  validation {
    condition     = contains(["learning", "development", "staging"], var.environment)
    error_message = "Lab không chấp nhận production."
  }
}

resource "terraform_data" "deployment_contract" {
  input = {
    environment = var.environment
    owner       = "devops-learning"
    managed_by  = "terraform"
  }
}

output "deployment_contract" {
  value = terraform_data.deployment_contract.output
}

